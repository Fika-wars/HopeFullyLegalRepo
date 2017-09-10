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

public class MyMockPositioningProvider1 implements MockPositioningProvider {

    private int currentIndex = 0;
    private List<MockPositioningEvent> mockPositioningEvents = new ArrayList<>();
    private long mockLocationIntervalMs = 800;
    private long mockLocktionInterval3  = 7000;
    private long mockLocktionInterval2  = 300000;
    private long mockLocktionInterval1  = 4000;
    private long mockHeadingIntervalMs = 300;
    private LocationSource mockLocationSource = new MyLocationSource();

    /**
     * @param buildingInfo
     */
    public MyMockPositioningProvider1(BuildingInfo buildingInfo) {
        this.mockPositioningEvents = createMockPositioningEvents(buildingInfo);
    }

    private List<MockPositioningEvent> createMockPositioningEvents(BuildingInfo buildingInfo) {

        FloorInfo floorInfo = buildingInfo.getFloorInfo(buildingInfo.getDefaultFloorId());

        double centerX = floorInfo.getxMaxPixels() / 2.0;
        double centerY = floorInfo.getyMaxPixels() / 2.0;

        double offsetX = centerX / 5;
        double offsetY = centerY / 5;

        FloorId floorId = new FloorId("1");
/*
        Location location  = createLocation(floorInfo, centerX, centerY, floorId, 4.5);
        Location location2 = createLocation(floorInfo, centerX + offsetX, centerY, floorId, 2.5);
        Location location3 = createLocation(floorInfo, centerX + offsetX, centerY + offsetY, floorId, 3);
        Location location4 = createLocation(floorInfo, centerX, centerY + offsetY, floorId, 5.5);
*/

        Location location1 = createLocation(floorInfo, 750, 3, floorId, 3.5);
        Location location2 = createLocation(floorInfo, 850, 100, floorId, 3);
        Location location3 = createLocation(floorInfo, 820, 200, floorId, 2.5);
        Location location4 = createLocation(floorInfo, 815, 280, floorId, 2);
        Location location5 = createLocation(floorInfo, 870, 283, floorId, 1.5);


        float startHeading = 0;
        Heading heading1 = new Heading(startHeading, new Date());
        Heading heading2 = new Heading(startHeading + 10, new Date());


        List<MockPositioningEvent> mockPositioningEvents = new ArrayList<>();

        mockPositioningEvents.add(new MockLocationAvailabilityEvent(LocationAvailability.Available, 0));
        mockPositioningEvents.add(new MockLocationEvent(location1, mockLocationIntervalMs));
        //mockPositioningEvents.add(new MockHeadingEvent(heading1, mockHeadingIntervalMs));
        mockPositioningEvents.add(new MockLocationEvent(location2, mockLocationIntervalMs));
        mockPositioningEvents.add(new MockLocationEvent(location3, mockLocktionInterval3));

        //Kaffemaskin
        mockPositioningEvents.add(new MockLocationEvent(location4, mockLocationIntervalMs));

        //Soffa
        mockPositioningEvents.add(new MockLocationEvent(location5, mockLocktionInterval1));
        mockPositioningEvents.add(new MockLocationEvent(location5, mockLocktionInterval2));

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
