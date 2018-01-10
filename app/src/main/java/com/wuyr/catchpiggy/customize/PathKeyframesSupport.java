package com.wuyr.catchpiggy.customize;

import android.graphics.PathMeasure;
import android.graphics.PointF;

import java.util.Collections;

/**
 * Created by wuyr on 17-11-22 上午12:45.
 */

/**
 * 关键帧,参考自SDK
 */
class PathKeyframesSupport extends Keyframes {

    private static final float PRECISION = 0.5f;
    private float[] mX;
    private float[] mY;
    private int numPoints;
    private boolean isFirstReverse;

    PathKeyframesSupport(MyPath path) {
        super(path);
        isFirstReverse = true;
    }

    @Override
    void init(MyPath path) {
        final PathMeasure pathMeasure = new PathMeasure(path, false);
        final float pathLength = pathMeasure.getLength();
        numPoints = (int) (pathLength / PRECISION) + 1;
        mX = new float[numPoints];
        mY = new float[numPoints];
        final float[] position = new float[2];
        for (int i = 0; i < numPoints; ++i) {
            final float distance = (i * pathLength) / (numPoints - 1);
            pathMeasure.getPosTan(distance, position, null /* tangent */);
            mX[i] = position[0];
            mY[i] = position[1];
        }
        mPath = path;
    }

    @Override
    PointF getValue(float fraction) {
        int index = (int) (numPoints * fraction);
        mTempPointF.set(mX[index], mY[index]);
        return mTempPointF;
    }

    @Override
    void reverse() {
        if (isFirstReverse) {
            super.reverse();
            isFirstReverse = false;
        } else {
            Collections.reverse(mPath.getData());
            float[] temp = new float[mX.length];
            for (int i = 0; i < mX.length; i++) {
                temp[i] = mX[mX.length - i - 1];
            }
            mX = temp;

            float[] temp2 = new float[mY.length];
            for (int i = 0; i < mY.length; i++) {
                temp2[i] = mY[mY.length - i - 1];
            }
            mY = temp2;
        }
    }
}
