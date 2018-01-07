package com.wuyr.catchpiggy.models;

/**
 * Created by wuyr on 17-10-16 下午11:56.
 */

public class WayData {
    public int count;
    public boolean isBlock;
    public int x, y;

    public WayData(int count, boolean isBlock, int x, int y) {
        this.count = count;
        this.isBlock = isBlock;
        this.x = x;
        this.y = y;
    }

    public WayData(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof WayData ? x == ((WayData) obj).x && y == ((WayData) obj).y : this == obj;
    }

    @Override
    public String toString() {
//        return "x: " + x + "\ty: " + y;
        return y + "," + x;
    }
}
