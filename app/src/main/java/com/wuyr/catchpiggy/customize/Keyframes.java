package com.wuyr.catchpiggy.customize;

import android.graphics.PointF;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

/**
 * Created by wuyr on 17-11-22 下午8:56.
 */

abstract class Keyframes {

    MyPath mPath;
    PointF mTempPointF;

    Keyframes(MyPath path) {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("The path must not be null or empty");
        }
        mTempPointF = new PointF();
        init(path);
    }

    void reverse() {
        List<PointF> data = mPath.getData();
        MyPath path = new MyPath();
        Collections.reverse(data);
        path.moveTo(data.get(0).x, data.get(0).y);
        Queue<PointF> queue = new ArrayDeque<>(data);
        while (!queue.isEmpty()) {
            PointF item = queue.poll();
            if (!queue.isEmpty()) {
                PointF item2 = queue.poll();
                path.quadTo(item.x, item.y, item2.x, item2.y);
            } else {
                path.lineTo(item.x, item.y);
            }
        }
        init(path);
    }

    abstract PointF getValue(float fraction);

    abstract void init(MyPath path);
}
