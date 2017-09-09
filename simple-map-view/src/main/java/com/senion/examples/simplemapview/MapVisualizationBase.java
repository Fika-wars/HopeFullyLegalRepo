package com.senion.examples.simplemapview;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.DisplayMetrics;
import android.view.animation.LinearInterpolator;

import java.util.List;

public abstract class MapVisualizationBase implements MapVisualization {

    protected static final int ANIMATION_TIME_MILLIS = 400;

    private boolean isEnabled;
    protected MapImageView mapView;

    public void init(MapImageView mapView) {
        this.mapView = mapView;
    }

    @Override
    public final void onDraw(Canvas canvas) {
        if (isEnabled()) {
            doDraw(canvas);
        }
    }

    protected void doDraw(Canvas canvas) {
    }

    @Override
    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    protected final Paint createPaint(int color, int alpha, float strokeWidth, Paint.Style style) {
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setAlpha(alpha);
        paint.setStrokeWidth(strokeWidth);
        paint.setStyle(style);
        return paint;
    }

    protected final int dpToPx(int dp) {
        DisplayMetrics displayMetrics = mapView.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    protected final void restartAnimator(ValueAnimator animator, float from, float to) {
        animator.cancel();
        animator.setFloatValues(from, (float)to);
        animator.start();
    }

    protected final void setupFloatValueAnimation(final ValueAnimator animator, final ValueAnimator.AnimatorUpdateListener listener) {
        animator.setDuration(ANIMATION_TIME_MILLIS);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                listener.onAnimationUpdate(valueAnimator);
                mapView.invalidate();
            }
        });
    }

    protected final void drawPath(Canvas canvas, List<ImagePoint> points, Paint paint, float pathWidthMeters) {
        Path path = new Path();

        paint.setStrokeWidth(pathWidthMeters * mapView.getPixelsPerMeter());

        int i = 0;

        for (ImagePoint point : points) {
            if (i == 0) {
                path.moveTo((float)point.getX(), (float)point.getY());
            } else {
                path.lineTo((float)point.getX(), (float)point.getY());
            }

            i++;
        }

        canvas.drawPath(path, paint);
    }
}
