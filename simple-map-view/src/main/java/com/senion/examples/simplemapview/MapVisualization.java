package com.senion.examples.simplemapview;

import android.graphics.Canvas;

public interface MapVisualization {
    void onDraw(Canvas canvas);

    void setEnabled(boolean isEnabled);

    boolean isEnabled();
}
