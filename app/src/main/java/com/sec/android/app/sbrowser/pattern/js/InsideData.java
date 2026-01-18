package com.sec.android.app.sbrowser.pattern.js;


import android.graphics.RectF;

public class InsideData {
    public int inside;
    public RectF rect;

    public boolean isInside() {
        return (inside == 0);
    }

    public int getHeight() {
        return (int) Math.abs((rect.bottom) - (rect.top));
    }
}
