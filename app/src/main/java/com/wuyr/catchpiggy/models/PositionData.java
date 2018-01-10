package com.wuyr.catchpiggy.models;

/**
 * Created by wuyr on 17-10-16 下午11:55.
 */

public class PositionData {
    public float startX;
    public float startY;
    public float endX;
    public float endY;

    @Override
    public String toString() {
        return startX + "," + startY + "," + endX + "," + endY;
    }
}
