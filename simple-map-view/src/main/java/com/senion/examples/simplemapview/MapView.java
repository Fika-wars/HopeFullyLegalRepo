package com.senion.examples.simplemapview;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.senion.examples.simplemapview.buildinginfo.*;
import com.senion.stepinside.sdk.*;

import java.util.ArrayList;

public class MapView extends FrameLayout {

    public int x_pos;
    public int y_pos;

    private MapImageView mapImageView;
    private Button floorUpButton;
    private Button floorDownButton;

    private BuildingInfo building = null;
    private FloorId currentFloorId = null;
    private Bitmap currentImage = null;

    private RectF screenRect = new RectF();

    private Button followModeButton;
    private Button compassButton;

    private ArrayList<MapViewListener> listeners = new ArrayList<>();

    public MapView(Context context) {
        super(context);
        init(context);
    }

    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MapView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_map, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        if (isInEditMode()) {
            return;
        }


        mapImageView = (MapImageView)findViewById(R.id.map);
        floorUpButton = (Button)findViewById(R.id.floor_up_button);
        floorDownButton = (Button)findViewById(R.id.floor_down_button);
        followModeButton = (Button)findViewById(R.id.center_position_button);
        compassButton = (Button)findViewById(R.id.compass_button);

        floorUpButton.setOnClickListener(floorButtonsListener);
        floorDownButton.setOnClickListener(floorButtonsListener);
        followModeButton.setOnClickListener(followModeButtonListener);
        compassButton.setOnClickListener(compassButtonListener);

        mapImageView.registerListener(mapImageViewListener);

        // mapImageView size is not available yet
        ViewTreeObserver viewTreeObserver = mapImageView.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mapImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    int width = mapImageView.getWidth();
                    int height = mapImageView.getHeight();

                    mapImageView.setScaleType(ImageView.ScaleType.MATRIX);
                    mapImageView.setScreenWidth(width * 2);
                    mapImageView.setScreenHeight(height * 2);

                    FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(width * 2, height * 2);
                    layoutParams.setMargins(-width / 2, -height / 2, width / 2, height / 2);
                    mapImageView.setLayoutParams(layoutParams);
                    mapImageView.invalidate();

                    // Compensate for mapImageView margins
                    screenRect.left = width * 0.5f;
                    screenRect.top = height * 0.5f;
                    screenRect.right = width * 1.5f;
                    screenRect.bottom = height * 1.5f;

                    recenterMapImage();
                }
            });
        }
    }

    public void setBuilding(BuildingInfo building) {
        this.building = building;

        refreshFloorControls();

        setFloor(building.getDefaultFloorId());
    }

    private void refreshFloorControls() {
        boolean showFloorControlButtons = building.getNumberOfFloors() > 1;
        floorUpButton.setVisibility(showFloorControlButtons ? View.VISIBLE : View.GONE);
        floorDownButton.setVisibility(showFloorControlButtons ? View.VISIBLE : View.GONE);
    }

    private void setFloor(FloorId floorId) {
        if ((currentFloorId != null && currentFloorId.equals(floorId)) || building == null) {
            return;
        }

        FloorId oldFloorId = currentFloorId;
        currentFloorId = floorId;

        FloorInfo floorInfo = building.getFloorInfo(currentFloorId);

        if (currentImage != null) {
            currentImage.recycle();
        }

        currentImage = building.getBitmap(currentFloorId, 800, 800);

        //mapImageView.setSampleScale(currentImage.getSampleSize());
        mapImageView.setPixelsPerMeter(((float)floorInfo.getPixelsPerMeter()));
        mapImageView.setImageBitmap(currentImage);

        correctScale(oldFloorId);
        translateImageAfterChange(oldFloorId);

        mapImageView.invalidate();

        for (MapViewListener listener : listeners) {
            listener.currentFloorChanged(oldFloorId, currentFloorId);
        }

        // TODO notify floor changed?
    }

    private void correctScale(FloorId oldFloorNr) {
        // Correct scale of new image so it is displayed in the same pixel per
        // meter ratio as the old image
        if (building != null && oldFloorNr != null) {
            double oldPixelPerMeter = building.getFloorInfo(oldFloorNr).getPixelsPerMeter();
            double newPixelPerMeter = building.getFloorInfo(currentFloorId).getPixelsPerMeter();
            float newScale = (float) (oldPixelPerMeter / newPixelPerMeter);
            mapImageView.changeScale(newScale);
        }
    }

    private void translateImageAfterChange(FloorId oldFloorNr) {
        if (oldFloorNr == null) {
            recenterMapImage();
        }

        if (currentFloorId != null && oldFloorNr != null) {

            ImagePoint pos = mapImageView.getPixelCoordinatesOfCenterOfCurrentImage();

            boolean recenterImage = !mapImageView.isPositionInsideScreen(pos);
            if (recenterImage) {
                recenterMapImage();
            } else {
                // Center new map around same long/lat as before.
                ImagePoint p = mapImageView.getPixelCoordinatesOfCenterOfCurrentImage();
                LocationCoordinates l = building.imagePointToLocationCoordinates(p, oldFloorNr);
                //l.setFloorNr(currentFloorNr); // TODO
                ImagePoint pNew = building.locationCoordinatesToImagePoint(l);
                mapImageView.translateScreenTo(pNew);
            }
        }
    }

    private void recenterMapImage() {
        if (currentImage == null) {
            return;
        }

        int imageHeight = currentImage.getHeight();
        int imageWidth = currentImage.getWidth();

        mapImageView.translateScreenTo(imageWidth / 2, imageHeight / 2);
    }

    public void centerOn(ImagePoint point) {
        centerOn((float)point.getX(), (float)point.getY());
    }

    public void centerOn(float posX, float posY) {
        mapImageView.translateScreenTo(posX, posY);
    }

    public FloorId getFloor() {
        return currentFloorId;
    }

    public void addListener(MapViewListener listener) {
        listeners.add(listener);
    }

    public void removeListener(MapViewListener listener) {
        listeners.remove(listener);
    }

    public void setHeading(Heading heading) {
        FloorInfo floorInfo = building.getFloorInfo(currentFloorId);
        double bitmapHeading = floorInfo.magneticHeadingToBitmapHeading(heading.getAngle());

        mapImageView.setHeading(bitmapHeading);
        compassButton.setRotation(mapImageView.getRotation() - 90);
    }

    public void setLocation(Location location) {
        FloorInfo floorInfo = building.getFloorInfo(currentFloorId);
        ImagePoint imagePoint = building.locationCoordinatesToImagePoint(location.getCoordinates());
        x_pos = (int) imagePoint.getX();
        y_pos = (int) imagePoint.getY();
        mapImageView.setPosition(imagePoint);
        mapImageView.setUncertaintyRadius(floorInfo.getPixelsPerMeter() * location.getUncertaintyRadius());
    }

    public void setLocationAvailability(LocationAvailability locationAvailability) {
        mapImageView.setLocationAvailability(locationAvailability);
    }

    private void setFollowMode(FollowMode followMode) {
        mapImageView.setFollowMode(followMode);

        updateFollowButton();
    }

    private void updateFollowButton() {
        switch (mapImageView.getFollowMode()) {
            case NONE:
                followModeButton.setBackgroundResource(R.drawable.map_cent_button);
                break;
            case CENTER:
                followModeButton.setBackgroundResource(R.drawable.map_cent_sel_button);
                break;
            case ROTATE:
                followModeButton.setBackgroundResource(R.drawable.map_cent_rot_button);
                break;
        }
    }

    public interface MapViewListener {
        void currentFloorChanged(FloorId oldFloorNr, FloorId newFloorNr);
    }

    private final OnClickListener followModeButtonListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (mapImageView.getFollowMode()) {
                case NONE:
                    setFollowMode(FollowMode.CENTER);
                    break;
                case CENTER:
                    setFollowMode(FollowMode.ROTATE);
                    break;
                case ROTATE:
                    setFollowMode(FollowMode.NONE);
                    break;
            }
        }
    };

    private final OnClickListener compassButtonListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mapImageView.getFollowMode() == FollowMode.ROTATE) {
                setFollowMode(FollowMode.CENTER);
            }

            if (mapImageView.getFollowMode() != FollowMode.ROTATE) {
                mapImageView.setRotation(90f);
            }
        }
    };

    private final OnClickListener floorButtonsListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view == floorUpButton) {
                setFloor(building.getNextFloorIdAbove(currentFloorId));
            } else if (view == floorDownButton) {
                setFloor(building.getNextFloorIdBelow(currentFloorId));
            }
        }
    };

    private final MapImageView.Listener mapImageViewListener = new MapImageView.Listener() {
        @Override
        public void onDragged() {
            setFollowMode(FollowMode.NONE);
        }
    };
}
