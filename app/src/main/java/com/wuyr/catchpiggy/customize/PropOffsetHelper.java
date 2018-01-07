package com.wuyr.catchpiggy.customize;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.SystemClock;

import com.wuyr.catchpiggy.models.PropData;
import com.wuyr.catchpiggy.utils.ThreadPool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by wuyr on 17-12-8 上午2:50.
 */

public class PropOffsetHelper {

    private float mPropOffsetSpeed;
    private MyDrawable mPropDrawable;
    private List<PropData> mProps;
    private List<Integer> mLeavedProps;
    private float mStartX, mStartY;
    private int mPropSize;
    private Future mUpdateTask;
    private float mLeftOffset;
    private volatile boolean isNeed;
    private long mLastStopTime;

    public PropOffsetHelper(Bitmap propBitmap, int width, int height, int propSize) {
        mPropDrawable = new MyDrawable(0, propBitmap);
        mProps = new ArrayList<>();
        mLeftOffset = (propSize - mPropDrawable.getIntrinsicWidth()) / 2;
        mStartX = width + mLeftOffset;
        mStartY = height - mPropDrawable.getIntrinsicHeight();
        mPropSize = propSize;
        mLeavedProps = new ArrayList<>();
        long propOffsetAnimationDuration = 4000L;
        mPropOffsetSpeed = mStartX / propOffsetAnimationDuration;
    }

    public MyDrawable getPropDrawable(int index) {
        return mProps.get(index).drawable;
    }

    public boolean isPropLeaved(int index) {
        return mLeavedProps.contains(index);
    }

    public int getLeavedPropCount() {
        return mLeavedProps.size();
    }

    public void addProp() {
        PropData data = new PropData(mPropDrawable.clone());
        data.setY(mStartY);
        data.setX(mStartX);
        mProps.add(data);
        //LogUtil.print(mProps.size());
    }

    public void setX(int index, float x) {
        mProps.get(index).setX(x);
    }

    public void setY(int index, float y) {
        mProps.get(index).setY(y);
    }

    public void propLeaved(int index) {
        mLeavedProps.add(index);
    }

    public void drawInQueueProps(Canvas canvas) {
        for (int i = 0; i < mProps.size(); i++) {
            if (!mLeavedProps.contains(i)) {
                mProps.get(i).draw(canvas);
                //LogUtil.print(mProps.get(i));
            }
        }
    }

    public void drawLeavedProps(Canvas canvas) {
        for (int i = 0; i < mLeavedProps.size(); i++) {
            mProps.get(mLeavedProps.get(i)).draw(canvas);
            //LogUtil.print(mProps.get(mLeavedProps.get(i)));
        }
    }

    public int size() {
        return mProps.size();
    }

    public int getPropHeight() {
        return mPropDrawable.getIntrinsicHeight();
    }

    public int getPropWidth() {
        return mPropDrawable.getIntrinsicWidth();
    }

    public void stop() {
        isNeed = false;
        if (mUpdateTask != null) {
            mUpdateTask.cancel(true);
            mUpdateTask = null;
        }
        mLastStopTime = SystemClock.uptimeMillis();
    }

    public int getQueueSize() {
        return mProps.size() - mLeavedProps.size();
    }

    public void restart() {
        updatePropGenerateTime();
        isNeed = true;
        mUpdateTask = ThreadPool.getInstance().execute(() -> {
            boolean isFinished;
            float distance, offset;
            int hitOffsetCount;
            long intervalTime, updateTime;
            while (isNeed) {
                for (int i = 0; i < mProps.size(); i++) {
                    PropData prop = mProps.get(i);
                    if (mLeavedProps.contains(i)) {
                        continue;
                    }
                    distance = i * mPropSize + mLeftOffset;
                    hitOffsetCount = 0;
                    for (int j = 0; j < mLeavedProps.size(); j++) {
                        if (mLeavedProps.get(j) < i) {
                            hitOffsetCount++;
                        }
                    }
                    distance -= mPropSize * hitOffsetCount;
                    isFinished = prop.getX() <= distance;
                    updateTime = SystemClock.uptimeMillis();
                    if (!isFinished) {
                        intervalTime = updateTime - prop.lastUpdateTime;
                        if (intervalTime > 10) {
                            offset = intervalTime * mPropOffsetSpeed;
                            prop.setX(prop.getX() - offset);
                            prop.lastUpdateTime = updateTime;
                        }
                    } else {
                        prop.lastUpdateTime = updateTime;
                    }
                }
            }
        });
    }

    private void updatePropGenerateTime() {
        if (mLastStopTime > 0) {
            long totalStoppedTime = SystemClock.uptimeMillis() - mLastStopTime;
            mLastStopTime = 0;
            for (int i = 0; i < mProps.size(); i++) {
                mProps.get(i).lastUpdateTime += totalStoppedTime;
            }
        }
    }

    public void release() {
        stop();
        if (mPropDrawable != null) {
            mPropDrawable.release();
            mPropDrawable = null;
        }
        if (mProps != null) {
            for (PropData propData : mProps) {
                if (propData != null) {
                    propData.release();
                }
            }
        }
        mProps = null;
        mLeavedProps = null;
    }
}
