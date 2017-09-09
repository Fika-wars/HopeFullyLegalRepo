package com.senion.examples.simplemapview;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import com.senion.stepinside.sdk.LocationAvailability;

public class PositionAndHeadingMapVisualization extends MapVisualizationBase {
    private static final float POSITION_DIAMETER_METERS = 2f;
    private static final int MIN_POSITION_DIAMETER_DP = 8;
    private static final int MIN_POSITION_STROKE_DP = 3;
    private static final int UNCERTAINTY_RADIUS_CIRCLE_STROKE_DP = 1;

    private final ValueAnimator posXAnimator = ValueAnimator.ofFloat();
    private final ValueAnimator posYAnimator = ValueAnimator.ofFloat();
    private final ValueAnimator headingAnimator = ValueAnimator.ofFloat();
    private final ValueAnimator uncertaintyRadiusAnimator = ValueAnimator.ofFloat();

    private ImagePoint pos;
    private LocationAvailability locationAvailability = LocationAvailability.NotAvailable;

    private float posX;
    private float posY;
    private float heading;
    private float uncertaintyRadius;

    private boolean hasSetHeading = false;

    private FollowMode followMode = FollowMode.ROTATE;

    private Paint uncertaintyCircleFillPaint;
    private Paint uncertaintyCircleStrokePaint;
    private Paint positionFillPaint;
    private Paint positionStrokePaint;
    private Paint headingFillPaint;
    private Paint headingStrokePaint;
    private int lightBlueColor;
    private int locationUnavailableUncertaintyCircleStrokeColor;
    private int locationUnavailableUncertaintyCircleFillColor;
    private int locationUnavailablePositionFillColor;

    @Override
    public void init(MapImageView mapView) {
        super.init(mapView);

        createPaint();
    }

    public PositionAndHeadingMapVisualization() {
        setupFloatValueAnimation(posXAnimator, new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                posX = (Float)valueAnimator.getAnimatedValue();
            }
        });

        setupFloatValueAnimation(posYAnimator, new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                posY = (Float)valueAnimator.getAnimatedValue();
            }
        });

        setupFloatValueAnimation(headingAnimator, new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                heading = (Float)valueAnimator.getAnimatedValue();
            }
        });

        setupFloatValueAnimation(uncertaintyRadiusAnimator, new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                uncertaintyRadius = (Float)valueAnimator.getAnimatedValue();
            }
        });
    }

    private void createPaint() {
        lightBlueColor = mapView.getContext().getResources().getColor(R.color.sl_blue);

        locationUnavailableUncertaintyCircleStrokeColor = mapView.getContext().getResources().getColor(R.color.location_unavailable_uncertainty_circle_stroke);
        locationUnavailableUncertaintyCircleFillColor = mapView.getContext().getResources().getColor(R.color.location_unavailable_uncertainty_circle_fill);
        locationUnavailablePositionFillColor = mapView.getContext().getResources().getColor(R.color.location_unavailable_position_fill);

        uncertaintyCircleFillPaint = createPaint(lightBlueColor, (int)(0.2*255), 0, Paint.Style.FILL);
        uncertaintyCircleStrokePaint = createPaint(lightBlueColor, 255, 1.0f, Paint.Style.STROKE);

        positionFillPaint = createPaint(lightBlueColor, 255, 3.0f, Paint.Style.FILL);
        positionFillPaint.setAntiAlias(true);

        positionStrokePaint = createPaint(Color.WHITE, 255, 3.0f, Paint.Style.STROKE);
        positionStrokePaint.setAntiAlias(true);

        headingFillPaint = positionFillPaint;

        headingStrokePaint = createPaint(positionStrokePaint.getColor(), positionStrokePaint.getAlpha(), positionStrokePaint.getStrokeWidth() / 2, Paint.Style.STROKE);
        headingStrokePaint.setAntiAlias(positionStrokePaint.isAntiAlias());
    }

    public void setFollowMode(FollowMode followMode) {
        this.followMode = followMode;
        mapView.invalidate();
    }

    public FollowMode getFollowMode() {
        return followMode;
    }

    public void setPos(ImagePoint pos) {
        this.pos = pos;

        restartAnimator(posXAnimator, posX, (float)pos.getX());
        restartAnimator(posYAnimator, posY, (float)pos.getY());
    }

    public ImagePoint getPos() {
        return pos;
    }

    public void setLocationAvailability(LocationAvailability locationAvailability) {
        this.locationAvailability = locationAvailability;
    }

    public void setHeading(float heading) {
        hasSetHeading = true;

        float end = heading % 360;
        float start = this.heading;
        float shortestAngle=((((end - start) % 360) + 540) % 360) - 180; // Ensure we don't spin when going from 360-0 or 0-360.

        float newHeading = this.heading + shortestAngle;

        restartAnimator(headingAnimator, this.heading, newHeading);
    }

    public void setUncertaintyRadius(float uncertaintyRadius) {
        restartAnimator(uncertaintyRadiusAnimator, this.uncertaintyRadius, uncertaintyRadius);
    }

    @Override
    protected void doDraw(Canvas canvas) {
        if (followMode == FollowMode.CENTER ||
            followMode == FollowMode.ROTATE) {
            mapView.translateScreenTo(posX, posY);
        }

        if (followMode == FollowMode.ROTATE) {
            float rotation = -heading - 90;
            mapView.setRotation(rotation);
        }

        if (pos != null) {
            drawUncertaintyRadiusCircle(canvas);
            if (hasSetHeading) {
                drawHeading(canvas);
            }
            drawPosition(canvas);
        }
    }

    private void drawUncertaintyRadiusCircle(Canvas canvas) {
        if (locationAvailability == LocationAvailability.Available) {
            uncertaintyCircleStrokePaint.setColor(lightBlueColor);
            uncertaintyCircleFillPaint.setColor(lightBlueColor);
            uncertaintyCircleFillPaint.setAlpha((int)(0.2*255));
        } else {
            uncertaintyCircleStrokePaint.setColor(locationUnavailableUncertaintyCircleStrokeColor);
            uncertaintyCircleFillPaint.setColor(locationUnavailableUncertaintyCircleFillColor);
        }

        uncertaintyCircleStrokePaint.setStrokeWidth(dpToPx(UNCERTAINTY_RADIUS_CIRCLE_STROKE_DP) / mapView.getTotalScale());

        canvas.drawCircle(posX, posY, uncertaintyRadius, uncertaintyCircleFillPaint);
        canvas.drawCircle(posX, posY, uncertaintyRadius, uncertaintyCircleStrokePaint);
    }

    private void drawPosition(Canvas canvas) {
        if (locationAvailability == LocationAvailability.Available) {
            positionFillPaint.setColor(lightBlueColor);
        } else {
            positionFillPaint.setColor(locationUnavailablePositionFillColor);
        }

        float minPositionDiameterPx = dpToPx(MIN_POSITION_DIAMETER_DP) / mapView.getTotalScale();
        float minPositionStrokeWidthPx = dpToPx(MIN_POSITION_STROKE_DP) / mapView.getTotalScale();

        float positionRadiusPx = (POSITION_DIAMETER_METERS / 2) * mapView.getPixelsPerMeter();
        float positionStrokeWidthPx = 0.10f * mapView.getPixelsPerMeter();

        positionStrokeWidthPx = Math.max(positionStrokeWidthPx, minPositionStrokeWidthPx);
        positionRadiusPx = Math.max(positionRadiusPx, minPositionDiameterPx);

        positionStrokePaint.setStrokeWidth(positionStrokeWidthPx);

        canvas.drawCircle(posX, posY, positionRadiusPx, positionStrokePaint);
        canvas.drawCircle(posX, posY, positionRadiusPx, positionFillPaint);
    }

    private void drawHeading(Canvas canvas) {
        float minPositionDiameterPx = dpToPx(MIN_POSITION_DIAMETER_DP) / mapView.getTotalScale();

        float positionRadiusPx = (POSITION_DIAMETER_METERS / 2) * mapView.getPixelsPerMeter();
        positionRadiusPx = Math.max(positionRadiusPx, minPositionDiameterPx);

        headingStrokePaint.setStrokeWidth(positionStrokePaint.getStrokeWidth() / 2f);

        float headingLengthPx = positionRadiusPx * 2;
        float headingWidthPx = headingLengthPx / 2;

        float Lx = (float) (posX + (headingWidthPx * Math.cos((90 + heading) * Math.PI / 180)));
        float Ly = (float) (posY + (headingWidthPx * Math.sin((90 + heading) * Math.PI / 180)));

        float Rx = (float) (posX + (-headingWidthPx * Math.cos((90 + heading) * Math.PI / 180)));
        float Ry = (float) (posY + (-headingWidthPx * Math.sin((90 + heading) * Math.PI / 180)));

        float Dx = (float) (posX + (headingLengthPx * Math.cos(heading * Math.PI / 180)));
        float Dy = (float) (posY + (headingLengthPx * Math.sin(heading * Math.PI / 180)));

        Path headingPath = new Path();
        headingPath.moveTo(Lx, Ly);
        headingPath.lineTo(Dx, Dy);
        headingPath.lineTo(Rx, Ry);

        canvas.drawPath(headingPath, headingFillPaint);
        canvas.drawLine(Lx, Ly, Dx, Dy, headingStrokePaint); // Use drawLine(s) instead of drawPath to avoid fuzziness
        canvas.drawLine(Dx, Dy, Rx, Ry, headingStrokePaint); // caused by drawPath not being hardware accelerated
    }
}
