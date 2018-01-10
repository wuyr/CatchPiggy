package com.wuyr.catchpiggy.models;

/**
 * Created by wuyr on 17-10-16 下午11:56.
 */

public class WayData {
    public int count;//方向上空闲状态的格子数
    public boolean isBlock;//中间是否有障碍
    public int x, y;//位置

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
