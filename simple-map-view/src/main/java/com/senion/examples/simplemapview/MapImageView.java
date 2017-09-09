package com.senion.examples.simplemapview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import com.senion.stepinside.sdk.LocationAvailability;

import java.util.concurrent.Callable;


public class MapImageView extends ImageView {

    private Listener listener;

    private GestureDetector gestureDetector;

    private Matrix matrix;
    private float totalScale = 1;
    private float[] f;

    private int screenWidth;
    private int screenHeight;
    private float pixelsPerMeter;

    private final PositionAndHeadingMapVisualization positionAndHeadingMapVisualization = new PositionAndHeadingMapVisualization();
    private MapDragZoomController dragZoomController;

    public MapImageView(Context context) {
        super(context);
        init(context);
    }

    public MapImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MapImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        positionAndHeadingMapVisualization.init(this);
        positionAndHeadingMapVisualization.setEnabled(true);

        dragZoomController = new MapDragZoomController(this, new Callable<ImagePoint>() {
            @Override
            public ImagePoint call() throws Exception {
                return getFollowPoint();
            }
        });

        matrix = new Matrix();

        f = new float[9];

        createGestureDetector(context);
    }

    private void createGestureDetector(Context context) {
        gestureDetector = new GestureDetector(context, new SimpleOnGestureListener());

        this.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        dragZoomController.onGestureActionDown(event);
                        break;
                    case MotionEvent.ACTION_UP:
                        dragZoomController.onGestureActionUp();
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        dragZoomController.onGestureActionPointerUp();
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        dragZoomController.onGestureActionPointerDown(event);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        dragZoomController.onGestureActionMove(event);
                        break;
                }

                gestureDetector.onTouchEvent(event);
                return true;
            }
        });
    }

    public float getTotalScale() {
        return dragZoomController.getTotalScale();
    }

    public void setFollowMode(FollowMode followMode) {
        positionAndHeadingMapVisualization.setFollowMode(followMode);
    }

    public FollowMode getFollowMode() {
        return positionAndHeadingMapVisualization.getFollowMode();
    }

    public ImagePoint getFollowPoint() {
        if (getFollowMode() != FollowMode.NONE) {
            return getPos();
        }

        return null;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        matrix.getValues(f);

        canvas.save();
        canvas.translate(f[Matrix.MTRANS_X], f[Matrix.MTRANS_Y]);
        canvas.scale(totalScale, totalScale);

        positionAndHeadingMapVisualization.onDraw(canvas);

        canvas.restore();
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    public void translateScreenTo(float posX, float posY) {
        matrix.getValues(f);
        int x = (int) (posX * totalScale + f[Matrix.MTRANS_X] - screenWidth / 2);
        int y = (int) (posY * totalScale + f[Matrix.MTRANS_Y] - screenHeight / 2);

        matrix.postTranslate(-x, -y);
        setImageMatrix(matrix);
    }

    public void translateScreenTo(ImagePoint p) {
        translateScreenTo((float) p.getX(), (float) p.getY());
    }

    public void changeScale(float scaleFactor) {
        matrix.postScale(scaleFactor, scaleFactor, (float) (screenWidth / 2), (float) (screenHeight / 2));
        matrix.getValues(f);
        totalScale = f[Matrix.MSCALE_X];
        setImageMatrix(matrix);
    }

    public ImagePoint getPixelCoordinatesOfCenterOfCurrentImage() {
        matrix.getValues(f);
        float x = (screenWidth / 2 - f[Matrix.MTRANS_X]) / totalScale;
        float y = (screenHeight / 2 - f[Matrix.MTRANS_Y]) / totalScale;
        return new ImagePoint(x, y);
    }

    public boolean isPositionInsideScreen(ImagePoint p) {
        matrix.getValues(f);
        double xPix = p.getX() * totalScale + f[Matrix.MTRANS_X];
        double yPix = p.getY() * totalScale + f[Matrix.MTRANS_Y];

        if (xPix > screenWidth || yPix > screenHeight || xPix < 0 || yPix < 0)
            return false;
        else
            return true;
    }

    public void setPosition(ImagePoint pos) {
        positionAndHeadingMapVisualization.setPos(pos);
    }

    public ImagePoint getPos() {
        return positionAndHeadingMapVisualization.getPos();
    }

    public void setPixelsPerMeter(float pixelsPerMeter) {
        this.pixelsPerMeter = pixelsPerMeter;
    }

    public float getPixelsPerMeter() {
        return pixelsPerMeter;
    }

    public void setUncertaintyRadius(double uncertaintyRadius) {
        positionAndHeadingMapVisualization.setUncertaintyRadius((float) uncertaintyRadius);
    }

    public void setHeading(double heading) {
        positionAndHeadingMapVisualization.setHeading((float) heading);
    }

    public void setScreenWidth(int screenWidth) {
        this.screenWidth = screenWidth;
    }

    public void setScreenHeight(int screenHeight) {
        this.screenHeight = screenHeight;
    }

    public void setLocationAvailability(LocationAvailability locationAvailability) {
        positionAndHeadingMapVisualization.setLocationAvailability(locationAvailability);
    }

    public interface Listener {
        void onDragged();
    }

    public void registerListener(Listener listener) {
        this.listener = listener;
    }

    public void unregisterListener() {
        this.listener = null;
    }

    private static class MapDragZoomController {
        private final MapImageView mapView;
        private final Callable<ImagePoint> followPointGetter;

        private final Matrix savedMatrix = new Matrix();
        private final Matrix dragZoomMatrix = new Matrix();

        private enum Mode {
            NONE,
            DRAG,
            ZOOM
        }

        private Mode mode = Mode.NONE;

        private float dragStartX;
        private float dragStartY;

        private float oldDist;
        private float zoomMidX;
        private float zoomMidY;
        private float zoomScale;
        private float totalScale;

        private MapDragZoomController(MapImageView mapView, Callable<ImagePoint> followPointGetter) {
            this.mapView = mapView;
            this.followPointGetter = followPointGetter;

            totalScale = mapView.totalScale;
        }

        private float getTotalScale() {
            return totalScale;
        }

        private boolean onGestureActionDown(MotionEvent event) {
            dragZoomMatrix.set(mapView.matrix);
            savedMatrix.set(dragZoomMatrix);

            dragStartX = event.getX();
            dragStartY = event.getY();

            mode = Mode.DRAG;

            return true;
        }

        private boolean onGestureActionPointerDown(MotionEvent event) {
            if (event.getPointerCount() < 2) {
                mode = Mode.NONE;
                return false;
            }

            oldDist = getGestureSpacing(event);
            if (oldDist > 10f) {
                zoomMidX = (event.getX(0) + event.getX(1)) / 2;
                zoomMidY = (event.getY(0) + event.getY(1)) / 2;
                dragZoomMatrix.set(mapView.matrix);
                savedMatrix.set(mapView.matrix);

                mode = Mode.ZOOM;

                return true;
            }

            mode = Mode.NONE;
            return false;
        }

        private boolean onGestureActionMove(MotionEvent event) {
            switch (mode) {
                case ZOOM:
                    return onZoomGesture(event);
                case DRAG:
                    return onDragGesture(event);
                default:
                    return false;
            }
        }

        private boolean onGestureActionPointerUp() {
            return onGestureActionUp();
        }

        private boolean onGestureActionUp() {
            if (mode != Mode.NONE) {
                mode = Mode.NONE;
                return true;
            }

            return false;
        }

        private boolean onDragGesture(MotionEvent event) {
            dragZoomMatrix.set(savedMatrix);
            float dX = event.getX() - dragStartX;
            float dY = event.getY() - dragStartY;

            if (dX < 10f && dY < 10f) {
                return false;
            }

            dragZoomMatrix.postTranslate(dX, dY);

            mapView.matrix.set(dragZoomMatrix);
            mapView.setImageMatrix(dragZoomMatrix);

            if (mapView.listener != null) {
                mapView.listener.onDragged();
            }

            return true;
        }

        private boolean onZoomGesture(MotionEvent event) {
            float newDist = getGestureSpacing(event);
            if (newDist < 10f) {
                return false;
            }

            try {
                ImagePoint followPoint = followPointGetter.call();

                doZoom(newDist, event, followPoint);
            } catch (Exception ignored) {
            }

            return true;
        }

        private void doZoom(float newDist, MotionEvent event, ImagePoint followPoint) {
            float postScaleX;
            float postScaleY;
            float postTransX;
            float postTransY;

            if (followPoint != null) {
                postScaleX = mapView.getWidth() / 2;
                postScaleY = mapView.getHeight() / 2;
                postTransX = 0;
                postTransY = 0;
            } else {
                postScaleX = zoomMidX;
                postScaleY = zoomMidY;
                postTransX = ((event.getX(0) + event.getX(1)) / 2) - zoomMidX;
                postTransY = ((event.getY(0) + event.getY(1)) / 2) - zoomMidY;
            }

            float sB = zoomScale;
            float tsB = totalScale;
            float[] mVal = new float[9];
            dragZoomMatrix.getValues(mVal);

            dragZoomMatrix.set(savedMatrix);
            zoomScale = newDist / oldDist;

            dragZoomMatrix.postScale(zoomScale, zoomScale, postScaleX, postScaleY);

            dragZoomMatrix.getValues(mapView.f);
            totalScale = mapView.f[Matrix.MSCALE_X];

            dragZoomMatrix.postTranslate(postTransX, postTransY);

            // Clamp zoom levels
            if (totalScale < 0.3f) {
                totalScale = tsB;
                zoomScale = sB;
                dragZoomMatrix.setValues(mVal);
            } else if (totalScale > 20f) {
                totalScale = tsB;
                zoomScale = sB;
                dragZoomMatrix.setValues(mVal);
            } else {
                mapView.setImageMatrix(dragZoomMatrix);
            }

            mapView.matrix.set(dragZoomMatrix);
            mapView.totalScale = totalScale;

            if (followPoint != null) {
                mapView.translateScreenTo(followPoint);
            }
        }

        private float getGestureSpacing(MotionEvent event) {
            float x = event.getX(0) - event.getX(1);
            float y = event.getY(0) - event.getY(1);

            return (float) Math.sqrt(x * x + y * y);
        }
    }
}
