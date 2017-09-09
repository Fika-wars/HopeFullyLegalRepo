package com.senion.examples.simplemapview.buildinginfo;

import com.senion.examples.simplemapview.ImagePoint;
import com.senion.stepinside.sdk.FloorId;
import com.senion.stepinside.sdk.LocationCoordinates;

public class FloorInfo {
    private final FloorId floorId;
    private final int ordinal;
    private final String floorName;
    private final String bitmapFilename;
    private final double latitude;
    private final double longitude;
    private final double xOffset;
    private final double yOffset;
    private final double bitmapOrientation;
    private final double pixelsPerMeter;
    private final int xMaxPixels;
    private final int yMaxPixels;

    private final static double a = 6378137; /* Equatorial earth radius */
    private final static double b = 6356752.314245; /* Polar earth radius */
    private final static double r = 0.5*(a+b); /* Average earth radius */

    public FloorInfo(
            FloorId floorId,
            int ordinal,
            String floorName,
            String bitmapFilename,
            double latitude,
            double longitude,
            double xOffset,
            double yOffset,
            double bitmapOrientation,
            double pixelsPerMeter,
            int xMaxPixels,
            int yMaxPixels) {

        this.floorId = floorId;
        this.ordinal = ordinal;
        this.floorName = floorName;
        this.bitmapFilename = bitmapFilename;
        this.latitude = latitude;
        this.longitude = longitude;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.bitmapOrientation = bitmapOrientation;
        this.pixelsPerMeter = pixelsPerMeter;
        this.xMaxPixels = xMaxPixels;
        this.yMaxPixels = yMaxPixels;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public double getPixelsPerMeter() {
        return pixelsPerMeter;
    }

    public FloorId getFloorId() {
        return floorId;
    }

    public String getBitmapFilename() {
        return bitmapFilename;
    }

    public double getBitmapOrientation() {
        return bitmapOrientation;
    }

    public double magneticHeadingToBitmapHeading(double angle) {
        return -90 - bitmapOrientation + angle;
    }

    public ImagePoint locationCoordinatesToImagePoint(LocationCoordinates locationCoordinates) {
        // This function makes a local approximation and should not be used over long distances
        LocationCoordinates origin = new LocationCoordinates(latitude, longitude, locationCoordinates.getFloorId());
        MetricPoint metricNW = locationCoordinatesToMetricPoint(locationCoordinates, origin);

        double orientationRad = toRad(bitmapOrientation);

        double deltaX = Math.cos(orientationRad) * metricNW.x - Math.sin(orientationRad) * metricNW.y;
        double deltaY = Math.sin(orientationRad) * metricNW.x + Math.cos(orientationRad) * metricNW.y;

        double x = -deltaY*pixelsPerMeter + this.xOffset;
        double y = -deltaX*pixelsPerMeter + this.yOffset;

        return new ImagePoint(x,y);
    }

    private static MetricPoint locationCoordinatesToMetricPoint(LocationCoordinates location, LocationCoordinates origin) {
        // This function makes a local approximation and should not be used over long distances
        double deltaN = r * toRad(location.getLatitude() - origin.getLatitude());
        double deltaW = r * toRad(-(location.getLongitude() - origin.getLongitude())) * Math.cos(toRad(origin.getLatitude()));

        return new MetricPoint(deltaN, deltaW);
    }

    public LocationCoordinates imagePointToLocationCoordinates(ImagePoint imagePoint, FloorId floorId) {
        // This function makes a local approximation and should not be used over long distances
        // Transform to a coordinate system in upper left corner of image
        double deltaX = (yOffset - imagePoint.getY()) / pixelsPerMeter;
        double deltaY = (xOffset - imagePoint.getX()) / pixelsPerMeter;

        // Rotate to NW-system
        double orientationRad = toRad(bitmapOrientation);
        double deltaN =  Math.cos(orientationRad) * deltaX + Math.sin(orientationRad) * deltaY;
        double deltaW = -Math.sin(orientationRad) * deltaX + Math.cos(orientationRad) * deltaY;

        MetricPoint metricNW = new MetricPoint(deltaN, deltaW);
        return metricPointToLocationCoordinates(metricNW, new LocationCoordinates(latitude, longitude, floorId));
    }

    private static LocationCoordinates metricPointToLocationCoordinates(MetricPoint metricNW, LocationCoordinates origin) {
        double lat = origin.getLatitude()  + 180.0 / Math.PI * metricNW.x / r;
        double lon = origin.getLongitude() - 180.0 / Math.PI * metricNW.y / (r * Math.cos(toRad(origin.getLatitude())));
        return new LocationCoordinates(lat, lon, origin.getFloorId());
    }

    private static double toRad(double deg) {
        return deg * Math.PI / 180.0;
    }

    private static class MetricPoint {
        public double x;
        public double y;

        public MetricPoint(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    public int getxMaxPixels() {
        return xMaxPixels;
    }

    public int getyMaxPixels() {
        return yMaxPixels;
    }
}
