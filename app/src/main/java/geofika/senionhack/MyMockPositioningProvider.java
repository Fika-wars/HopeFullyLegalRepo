package geofika.senionhack;

import com.senion.examples.simplemapview.ImagePoint;
import com.senion.examples.simplemapview.buildinginfo.BuildingInfo;
import com.senion.examples.simplemapview.buildinginfo.FloorInfo;
import com.senion.stepinside.sdk.FloorId;
import com.senion.stepinside.sdk.Heading;
import com.senion.stepinside.sdk.Location;
import com.senion.stepinside.sdk.LocationAvailability;
import com.senion.stepinside.sdk.LocationCoordinates;
import com.senion.stepinside.sdk.LocationSource;
import com.senion.stepinside.sdk.mocking.MockHeadingEvent;
import com.senion.stepinside.sdk.mocking.MockLocationAvailabilityEvent;
import com.senion.stepinside.sdk.mocking.MockLocationEvent;
import com.senion.stepinside.sdk.mocking.MockPositioningEvent;
import com.senion.stepinside.sdk.mocking.MockPositioningProvider;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MyMockPositioningProvider implements MockPositioningProvider {

    private int currentIndex = 0;
    private List<MockPositioningEvent> mockPositioningEvents = new ArrayList<>();
    private long mockLocationIntervalMs = 800;
    private long mockHeadingIntervalMs = 300;
    private LocationSource mockLocationSource = new MyLocationSource();

    /**
     * @param buildingInfo
     */
    public MyMockPositioningProvider(BuildingInfo buildingInfo) {
        this.mockPositioningEvents = createMockPositioningEvents(buildingInfo);
    }

    private List<MockPositioningEvent> createMockPositioningEvents(BuildingInfo buildingInfo) {

        FloorInfo floorInfo = buildingInfo.getFloorInfo(buildingInfo.getDefaultFloorId());

        double centerX = floorInfo.getxMaxPixels() / 2.0;
        double centerY = floorInfo.getyMaxPixels() / 2.0;

        double offsetX = centerX / 5;
        double offsetY = centerY / 5;

        FloorId floorId = new FloorId("1");

        Location location = createLocation(floorInfo, centerX, centerY, floorId, 4.5);
        Location location2 = createLocation(floorInfo, centerX + offsetX, centerY, floorId, 2.5);
        Location location3 = createLocation(floorInfo, centerX + offsetX, centerY + offsetY, floorId, 3);
        Location location4 = createLocation(floorInfo, centerX, centerY + offsetY, floorId, 5.5);

        float startHeading = 180;
        Heading heading = new Heading(startHeading, new Date());
        Heading heading2 = new Heading(startHeading - 90.0, new Date());

        List<MockPositioningEvent> mockPositioningEvents = new ArrayList<>();

        mockPositioningEvents.add(new MockLocationAvailabilityEvent(LocationAvailability.Available, 0));
        mockPositioningEvents.add(new MockLocationEvent(location, mockLocationIntervalMs));
        mockPositioningEvents.add(new MockHeadingEvent(heading, mockHeadingIntervalMs));
        mockPositioningEvents.add(new MockLocationEvent(location2, mockLocationIntervalMs));
        mockPositioningEvents.add(new MockLocationEvent(location3, mockLocationIntervalMs));
        mockPositioningEvents.add(new MockLocationAvailabilityEvent(LocationAvailability.NotAvailable, 0));
        mockPositioningEvents.add(new MockLocationEvent(location4, mockLocationIntervalMs));
        mockPositioningEvents.add(new MockHeadingEvent(heading, mockHeadingIntervalMs));
        mockPositioningEvents.add(new MockLocationAvailabilityEvent(LocationAvailability.Available, 0));
        mockPositioningEvents.add(new MockHeadingEvent(heading2, mockHeadingIntervalMs));

        return mockPositioningEvents;
    }

    private Location createLocation(FloorInfo floorInfo, double centerX, double centerY, FloorId floorId, double uncertaintyRadius) {
        ImagePoint centerImagePoint = new ImagePoint(centerX, centerY);
        LocationCoordinates centerLocation = floorInfo.imagePointToLocationCoordinates(centerImagePoint, floorId);

        return new Location(centerLocation.getLatitude(), centerLocation.getLongitude(), floorId, uncertaintyRadius, mockLocationSource, new Date());
    }


    /** {@inheritDoc} */
    @Override
    public MockPositioningEvent yieldNextMockPositioningEvent() {
        if (currentIndex >= mockPositioningEvents.size()) {
            currentIndex = 0;
        }

        MockPositioningEvent event = mockPositioningEvents.get(currentIndex);
        currentIndex++;

        return event;
    }

    public static class MyLocationSource implements LocationSource {
    }
}
