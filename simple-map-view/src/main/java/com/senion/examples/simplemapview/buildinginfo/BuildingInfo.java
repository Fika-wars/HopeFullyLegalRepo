package com.senion.examples.simplemapview.buildinginfo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.senion.examples.simplemapview.ImagePoint;
import com.senion.stepinside.sdk.FloorId;
import com.senion.stepinside.sdk.LocationCoordinates;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class BuildingInfo {
    private final List<FloorInfo> floorInfos = new ArrayList<>();
    private final Map<FloorId, FloorInfo> floorInfoMap = new HashMap<>();
    private final File file;

    public BuildingInfo(File file, List<FloorInfo> floorInfos) {
        this.file = file;
        this.floorInfos.addAll(floorInfos);

        for (FloorInfo floorInfo : floorInfos) {
            floorInfoMap.put(floorInfo.getFloorId(), floorInfo);
        }
    }

    public List<FloorInfo> getAllFloors() {
        return floorInfos;
    }

    public FloorId getDefaultFloorId() {
        return floorInfos.get(0).getFloorId();
    }

    public int getNumberOfFloors() {
        return floorInfos.size();
    }

    public FloorInfo getFloorInfo(FloorId floorId) {
        return floorInfoMap.get(floorId);
    }

    public Bitmap getBitmap(FloorId floorId, int w, int h) { // wh?
        return readBitmap(floorId);
    }

    public LocationCoordinates imagePointToLocationCoordinates(ImagePoint imagePoint, FloorId floorId) {
        return floorInfoMap.get(floorId).imagePointToLocationCoordinates(imagePoint, floorId);
    }

    public ImagePoint locationCoordinatesToImagePoint(LocationCoordinates locationCoordinates) {
        return floorInfoMap.get(locationCoordinates.getFloorId()).locationCoordinatesToImagePoint(locationCoordinates);
    }

    public FloorId getNextFloorIdAbove(FloorId floorId) {
        for (int i = 0; i < floorInfos.size(); i++) {
            FloorInfo floorInfo = floorInfos.get(i);
            if (floorInfo.getFloorId().equals(floorId)) {
                try {
                    return floorInfos.get(i + 1).getFloorId();
                } catch (Exception e) {
                    return null;
                }
            }
        }

        return null;
    }

    public FloorId getNextFloorIdBelow(FloorId floorId) {
        for (int i = 0; i < floorInfos.size(); i++) {
            FloorInfo floorInfo = floorInfos.get(i);
            if (floorInfo.getFloorId().equals(floorId)) {
                try {
                    return floorInfos.get(i - 1).getFloorId();
                } catch (Exception e) {
                    return null;
                }
            }
        }

        return null;
    }

    private Bitmap readBitmap(FloorId floorId) {
        InputStream is = null;
        ZipFile zf = null;
        try {
            zf = new ZipFile(file);
            String bitmapFilename = floorInfoMap.get(floorId).getBitmapFilename();
            ZipEntry e = zf.getEntry(bitmapFilename);
            is = zf.getInputStream(e);
            return BitmapFactory.decodeStream(is);
        } catch (Exception e) {
            return null;
        } finally {
            IOUtils.closeQuietly(is);
            if (zf != null) {
                try {
                    zf.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public static BuildingInfo read(Context context, InputStream inputStream) throws IOException {
        File file = new File(context.getFilesDir(), "temp-building-info" + UUID.randomUUID().toString() + ".zip"); // TODO based on asset name???
        FileOutputStream fos = new FileOutputStream(file);
        IOUtils.copy(inputStream, fos);
        IOUtils.closeQuietly(inputStream);
        IOUtils.closeQuietly(fos);

        BuildingInfo read = read(file);

        return read;
    }

    public static BuildingInfo read(InputStream inputStream) throws IOException {
        File file = File.createTempFile("temp-building-info", ".zip");
        FileOutputStream fos = new FileOutputStream(file);
        IOUtils.copy(inputStream, fos);
        IOUtils.closeQuietly(inputStream);
        IOUtils.closeQuietly(fos);

        return read(file);
    }

    public static BuildingInfo read(File file) throws IOException {
        String json = getBuildingInfoJson(file);

        ObjectMapper jackson = new ObjectMapper();
        jackson.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        BuildingInfoData buildingInfoData = jackson.readValue(json, BuildingInfoData.class);

        List<FloorInfo> floorInfos = new ArrayList<>();

        int ordinal = 0;
        for (BuildingInfoData.FloorInfo floorInfoData : buildingInfoData.floors) {
            floorInfos.add(createFloorInfo(floorInfoData, ordinal));
            ordinal++;
        }

        return new BuildingInfo(file, floorInfos);
    }

    private static FloorInfo createFloorInfo(BuildingInfoData.FloorInfo floorInfoData, int ordinal) {
        FloorId floorId = floorInfoData.floorId != null
                ? new FloorId(floorInfoData.floorId)
                : new FloorId(Integer.toString(floorInfoData.floorNr));

        return new FloorInfo(
                floorId,
                ordinal,
                floorInfoData.floorName,
                floorInfoData.bitmapFilename,
                floorInfoData.bitmapLocation.latitude,
                floorInfoData.bitmapLocation.longitude,
                floorInfoData.bitmapOffset.x,
                floorInfoData.bitmapOffset.y,
                floorInfoData.bitmapOrientation,
                floorInfoData.pixelsPerMeter,
                floorInfoData.xMaxPixels,
                floorInfoData.yMaxPixels);
    }

    private static String getBuildingInfoJson(File file) throws IOException {
        ZipFile zf = new ZipFile(file);

        String json = null;

        Enumeration<? extends ZipEntry> e = zf.entries();
        while (e.hasMoreElements()) {
            ZipEntry entry = e.nextElement();
            if (entry.getName().endsWith("_bi.json")) {
                InputStream inputStream = zf.getInputStream(entry);
                json = IOUtils.toString(inputStream, Charset.forName("UTF-8"));
                IOUtils.closeQuietly(inputStream);
                break;
            }
        }
        zf.close();

        if (json == null) {
            throw new IllegalArgumentException("Did not find the BuildingInfo json file in the zip file.");
        }

        return json;
    }

    private static class BuildingInfoData {

        public MapInfo mapInfo;
        public List<FloorInfo> floors;

        private static class MapInfo {
            public String name;
            public String dataDate;
            public String venueId;
            public String mapId;
            public String mapVersionId;
            public String versionName;
        }

        private static class FloorInfo {
            public String floorName;
            public int floorNr;
            public String floorId;
            public String bitmapFilename;
            public LongLat bitmapLocation;
            public Point bitmapOffset;
            public double bitmapOrientation;
            public double pixelsPerMeter;
            public int xMaxPixels;
            public int yMaxPixels;
        }

        private static class LongLat {
            public double latitude;
            public double longitude;
        }

        private static class Point {
            public double x;
            public double y;
        }
    }
}
