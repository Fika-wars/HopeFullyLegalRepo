package geofika.senionhack;

import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.senion.examples.simplemapview.buildinginfo.BuildingInfo;
import com.senion.stepinside.sdk.ApiKey;
import com.senion.stepinside.sdk.MapKey;
import com.senion.stepinside.sdk.StepInsideSdkManager;
import com.senion.stepinside.sdk.mocking.MockPositioningProvider;

public class SimpleMapExampleApplication extends MultiDexApplication {

    protected static String buildingInfoAssetName = "building-info-asset.zip";

    private final ApiKey apiKey = new ApiKey("CFZVVgYbGFFYFUwOD0hGSVsCAExMTxwAH0sYHU8bHhhOGUsU");
    private final MapKey mapKey = new MapKey("e52107e9-14ae-4acd-9dbe-2ef27adef383");

    private StepInsideSdkManager stepInsideSdkManager;

    @Override
    public void onCreate() {
        super.onCreate();

        BuildingInfo buildingInfo = null;
        try {
            buildingInfo = BuildingInfo.read(this, getApplicationContext().getAssets().open(buildingInfoAssetName));
        } catch (Exception e) {
            Log.e("ExampleApplication", "Error while loading BuildingInfo", e);
        }

       /* MockPositioningProvider mockLocationProvider =
                new MyMockPositioningProvider(buildingInfo); */

        stepInsideSdkManager = new StepInsideSdkManager.Builder(getApplicationContext())
                .withApiKey(apiKey)
                .withMapKey(mapKey)
                //.withMockPositioningProvider(mockLocationProvider)
                .enableGeoMessenger()
                .withLogLevel(Log.VERBOSE)
                .build();

        stepInsideSdkManager.initialize();
    }

    public StepInsideSdkManager getStepInsideSdkManager() {
        return stepInsideSdkManager;
    }
}
