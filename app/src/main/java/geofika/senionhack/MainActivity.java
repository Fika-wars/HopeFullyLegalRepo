package geofika.senionhack;

import android.Manifest;
import android.annotation.SuppressLint;
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
import com.senion.stepinside.sdk.Heading;
import com.senion.stepinside.sdk.Location;
import com.senion.stepinside.sdk.LocationAvailability;
import com.senion.stepinside.sdk.PositioningApi;
import com.senion.stepinside.sdk.StepInsideSdk;
import com.senion.stepinside.sdk.StepInsideSdkError;
import com.senion.stepinside.sdk.StepInsideSdkHandle;
import com.senion.stepinside.sdk.StepInsideSdkManager;
import com.senion.stepinside.sdk.Subscription;

public class MainActivity extends AppCompatActivity {

    private StepInsideSdkManager sdkManager;
    private StepInsideSdkHandle stepInsideSdk;

    private Subscription positioningSubscription;
    private Subscription statusSubscription;

    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapView = (MapView)findViewById(R.id.map_view);

        try {
            mapView.setBuilding(BuildingInfo.read(this, getAssets().open(SimpleMapExampleApplication.buildingInfoAssetName)));
        } catch (Exception e) {
            Log.e("MainActivity", "Error while loading BuildingInfo", e);
        }

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

    private void onAttachedToSdk(@NonNull StepInsideSdkHandle sdk) {
        stepInsideSdk = sdk;

        positioningSubscription = stepInsideSdk.positioning().addListener(positioningListener);
        statusSubscription = stepInsideSdk.addStatusListener(statusListener);

        stepInsideSdk.start();
    }

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

            updateLocation(location);
        }

        @Override
        public void onHeadingUpdated(@NonNull Heading heading) {
            if (isDestroyed()) return;

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
