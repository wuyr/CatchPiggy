package com.wuyr.catchpiggy.models;

import com.wuyr.catchpiggy.customize.views.Item;

/**
 * Created by wuyr on 17-10-16 下午11:57.
 */

public class WayData2 extends WayData{
    public Item item;

    public WayData2(Item item, int count, boolean isBlock) {
        super(count, isBlock, 0, 0);
        this.item = item;
    }

    @Override
    public String toString() {
        return "isBlock: " + isBlock + "\tcount: " + count;
    }
}
