package com.wuyr.catchpiggy.customize;

import android.annotation.SuppressLint;
import android.graphics.PointF;

/**
 * Created by wuyr on 17-11-22 上午12:45.
 */

/**
 * 关键帧,参考自SDK
 */
class PathKeyframes extends Keyframes {
    private static final int FRACTION_OFFSET = 0;
    private static final int X_OFFSET = 1;
    private static final int Y_OFFSET = 2;
    private static final int NUM_COMPONENTS = 3;

    private float[] mKeyframeData;

    PathKeyframes(MyPath path) {
        super(path);
    }

    private static float interpolate(float fraction, float startValue, float endValue) {
        float diff = endValue - startValue;
        return startValue + (diff * fraction);
    }

    @SuppressLint("NewApi")
    @Override
    void init(MyPath path) {
        mPath = path;
        mKeyframeData = path.approximate(.5F);
    }

    @Override
    PointF getValue(float fraction) {
        int numPoints = mKeyframeData.length / 3;
        if (fraction < 0) {
            return interpolateInRange(fraction, 0, 1);
        } else if (fraction > 1) {
            return interpolateInRange(fraction, numPoints - 2, numPoints - 1);
        } else if (fraction == 0) {
            return pointForIndex(0);
        } else if (fraction == 1) {
            return pointForIndex(numPoints - 1);
        } else {
            // Binary search for the correct section
            int low = 0;
            int high = numPoints - 1;

            while (low <= high) {
                int mid = (low + high) / 2;
                float midFraction = mKeyframeData[(mid * NUM_COMPONENTS) + FRACTION_OFFSET];

                if (fraction < midFraction) {
                    high = mid - 1;
                } else if (fraction > midFraction) {
                    low = mid + 1;
                } else {
                    return pointForIndex(mid);
                }
            }

            // now high is below the fraction and low is above the fraction
            return interpolateInRange(fraction, high, low);
        }
    }

    private PointF interpolateInRange(float fraction, int startIndex, int endIndex) {
        int startBase = (startIndex * NUM_COMPONENTS);
        int endBase = (endIndex * NUM_COMPONENTS);

        float startFraction = mKeyframeData[startBase + FRACTION_OFFSET];
        float endFraction = mKeyframeData[endBase + FRACTION_OFFSET];

        float intervalFraction = (fraction - startFraction) / (endFraction - startFraction);

        float startX = mKeyframeData[startBase + X_OFFSET];
        float endX = mKeyframeData[endBase + X_OFFSET];
        float startY = mKeyframeData[startBase + Y_OFFSET];
        float endY = mKeyframeData[endBase + Y_OFFSET];

        float x = interpolate(intervalFraction, startX, endX);
        float y = interpolate(intervalFraction, startY, endY);

        mTempPointF.set(x, y);
        return mTempPointF;
    }

    private PointF pointForIndex(int index) {
        int base = (index * NUM_COMPONENTS);
        int xOffset = base + X_OFFSET;
        int yOffset = base + Y_OFFSET;
        mTempPointF.set(mKeyframeData[xOffset], mKeyframeData[yOffset]);
        return mTempPointF;
    }
}
