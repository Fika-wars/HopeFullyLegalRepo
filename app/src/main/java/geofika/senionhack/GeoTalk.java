package geofika.senionhack;

import android.support.annotation.NonNull;

import com.senion.stepinside.sdk.GeoMessengerApi;
import com.senion.stepinside.sdk.GeoMessengerZone;
import com.senion.stepinside.sdk.Subscription;

import java.util.List;

/**
 * Created by Erik on 2017-09-09.
 */

public class GeoTalk implements GeoMessengerApi {

    @NonNull
    @Override
    public List<GeoMessengerZone> getCurrentZones() {
        List<GeoMessengerZone> L = getCurrentZones();
        return L;
    }

    @NonNull
    @Override
    public Subscription addListener(@NonNull Listener listener) {
        return null;
    }
}
