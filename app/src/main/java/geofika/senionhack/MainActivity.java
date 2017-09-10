package geofika.senionhack;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.senion.examples.simplemapview.MapView;
import com.senion.examples.simplemapview.buildinginfo.BuildingInfo;
import com.senion.stepinside.sdk.*;

import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {

    private StepInsideSdkManager sdkManager;
    private StepInsideSdkHandle stepInsideSdk;

    //Create text classes to write
    /*private TextView latitudeTextView;
    private TextView longitudeTextView;
    private TextView headingTextView;*/

    //Create geoMessenger
    private GeoMessengerApi geoMessengerApi;


    private Subscription positioningSubscription;
    private Subscription statusSubscription;

    private User mUser = null;
    private MyJsonRequest mJsonRequest = null;

    private MapView mapView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();

        mUser = (User)bundle.getSerializable("User");

        mJsonRequest = new MyJsonRequest(this,(String)bundle.get("Url"));

        mapView = (MapView)findViewById(R.id.map_view);

        try {
            mapView.setBuilding(BuildingInfo.read(this, getAssets().open(SimpleMapExampleApplication.buildingInfoAssetName)));
        } catch (Exception e) {
            Log.e("MainActivity", "Error while loading BuildingInfo", e);
        }

        /*latitudeTextView = (TextView)findViewById(R.id.latitudeTextView);
        longitudeTextView = (TextView)findViewById(R.id.longitudeTextView);
        headingTextView = (TextView)findViewById(R.id.headingTextView);*/

        sdkManager = ((SimpleMapExampleApplication)getApplication()).getStepInsideSdkManager();

        requestLocationPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();

        sdkManager.attachForeground(this, attachCallback);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (positioningSubscription != null) {
            positioningSubscription.unsubscribe();
        }

        if (statusSubscription != null) {
            statusSubscription.unsubscribe();
        }

        if (stepInsideSdk != null) {
            stepInsideSdk.stop();
            stepInsideSdk.detach();
        }
    }


    private void updateHeadingTextView(@NonNull Heading heading) {
        //headingTextView.setText(String.format("Heading: %s", heading.getAngle()));
    }

    private void updateLocationTextViews(@NonNull Location location) {
        //latitudeTextView.setText(String.format("Latitude: %s", location.getLatitude()));
        //longitudeTextView.setText(String.format("Longitude: %s", location.getLongitude()));
    }

    private void onAttachedToSdk(@NonNull StepInsideSdkHandle sdk) {
        stepInsideSdk = sdk;

        positioningSubscription = stepInsideSdk.positioning().addListener(positioningListener);
        statusSubscription = stepInsideSdk.addStatusListener(statusListener);

        stepInsideSdk.start();

        geoMessengerApi = stepInsideSdk.geoMessenger();
        geoMessengerApi.addListener(geoMessengerListener);
    }

    private Timer mTimer;
    private String TAG = "MainActivity";
    private GeoMessengerApi.Listener geoMessengerListener = new GeoMessengerApi.Listener()
    {
        @Override
        public void onZoneEntered(@NonNull GeoMessengerZone zone) {
            String zoneText = String.format("Entered zone %s", zone.getName());
            Toast.makeText(MainActivity.this, zoneText, Toast.LENGTH_LONG).show();

            mUser.setZone(zone.getId());

            mTimer = new Timer();
            mTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mJsonRequest.makeRequest(mUser);

                            HashMap<String, Integer> newList = mJsonRequest.getTeamList();
                            Log.d(TAG, "onZoneEntered: " + newList);
                        }
                    });
                }
            }, 0, 3000);
            



            List<GeoMessengerMessage> L = zone.getMessages();
            if (L.size() > 0) {
                String ZoneText = String.format("%s", L.get(0).getPayload());
                Toast.makeText(MainActivity.this, ZoneText, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onZoneExited(@NonNull GeoMessengerZone zone) {
            String zoneText = String.format("Exited zone %s", zone.getName());
            Toast.makeText(MainActivity.this, zoneText, Toast.LENGTH_LONG).show();

            if (mTimer != null){
                mTimer.cancel();
            }
        }
    };

    private void updateHeading(@NonNull Heading heading) {
        mapView.setHeading(heading);
    }

    private void updateLocation(@NonNull Location location) {
        mapView.setLocation(location);
    }

    private void updateLocationAvailability(LocationAvailability locationAvailability) {
        mapView.setLocationAvailability(locationAvailability);
    }

    @SuppressLint("DefaultLocale")
    private void showErrorToast(@NonNull StepInsideSdkError error) {
        String errorText = String.format("An error occurred: %s (%d)", error.toString(), error.getErrorCode());
        Toast.makeText(MainActivity.this, errorText, Toast.LENGTH_LONG).show();
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION }, 0);
        }
    }

    private final PositioningApi.Listener positioningListener = new PositioningApi.Listener() {
        @Override
        public void onLocationUpdated(@NonNull Location location) {
            if (isDestroyed()) return;

            updateLocationTextViews(location);
            updateLocation(location);
        }

        @Override
        public void onHeadingUpdated(@NonNull Heading heading) {
            if (isDestroyed()) return;

            updateHeadingTextView(heading);
            updateHeading(heading);
        }

        @Override
        public void onLocationAvailabilityUpdated(@NonNull LocationAvailability locationAvailability) {
            if (isDestroyed()) return;

            updateLocationAvailability(locationAvailability);
        }
    };

    private final StepInsideSdk.StatusListener statusListener = new StepInsideSdk.StatusListener() {
        @Override
        public void onError(@NonNull StepInsideSdkError error) {
            if (isDestroyed()) return;

            showErrorToast(error);
        }
    };

    private final StepInsideSdkManager.AttachCallback attachCallback = new StepInsideSdkManager.AttachCallback() {
        @Override
        public void onAttached(@NonNull StepInsideSdkHandle sdk) {
            if (isDestroyed()) return;

            onAttachedToSdk(sdk);
        }
    };
}
