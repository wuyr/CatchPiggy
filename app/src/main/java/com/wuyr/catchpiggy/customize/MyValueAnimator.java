package com.wuyr.catchpiggy.customize;

import android.os.SystemClock;

import com.wuyr.catchpiggy.utils.ThreadPool;

/**
 * Created by wuyr on 17-11-27 上午1:57.
 */

public class MyValueAnimator {
    private long duration;
    private Object[] mTargets;
    private OnAnimatorEndListener mOnAnimatorEndListener;
    private OnAnimatorMiddleListener mOnAnimatorMiddleListener;
    private volatile boolean isAnimationStopped;
    private float fromX, fromY, xPart, yPart;
    private boolean isRunOnCurrentThread, isOnAnimatorMiddleListenerCalled;
    private OnAnimatorUpdateListener mOnAnimatorUpdateListener;

    private MyValueAnimator(float fromX, float toX, float fromY, float toY, Object... targets) {
        mTargets = targets;
        this.fromX = fromX;
        this.fromY = fromY;
        xPart = toX - fromX;
        yPart = toY - fromY;
    }

    public static MyValueAnimator create(float fromX, float toX, float fromY, float toY, Object... targets) {
        return new MyValueAnimator(fromX, toX, fromY, toY, targets);
    }

    public void start() {
        stop();
        if (duration > 0) {
            if (isRunOnCurrentThread) {
                startAnimation();
            } else {
                ThreadPool.getInstance().execute(this::startAnimation);
            }
        }
    }

    private void startAnimation() {
        isAnimationStopped = false;
        final long startTime = SystemClock.uptimeMillis();
        long currentPlayedDuration;
        while ((currentPlayedDuration = SystemClock.uptimeMillis() - startTime) < duration) {
            if (isAnimationStopped) {
                break;
            }
            float progress = (float) currentPlayedDuration / (float) duration;
            if (!isOnAnimatorMiddleListenerCalled && mOnAnimatorMiddleListener != null && progress >= .5F) {
                isOnAnimatorMiddleListenerCalled = true;
                mOnAnimatorMiddleListener.onAnimationMiddle();
            }
            if (mOnAnimatorUpdateListener != null) {
                mOnAnimatorUpdateListener.onUpdate(progress);
            } else {
                update(progress);
            }
        }
        if (mOnAnimatorEndListener != null) {
            mOnAnimatorEndListener.onAnimationEnd();
        }
    }

    public MyValueAnimator setRunOnCurrentThread() {
        this.isRunOnCurrentThread = true;
        return this;
    }

    public void stop() {
        isAnimationStopped = true;
    }

    public boolean isAnimationPlaying() {
        return !isAnimationStopped;
    }

    public MyValueAnimator setDuration(long duration) {
        this.duration = duration;
        return this;
    }

    public MyValueAnimator setOnAnimatorEndListener(OnAnimatorEndListener listener) {
        this.mOnAnimatorEndListener = listener;
        return this;
    }

    public MyValueAnimator setOnAnimatorMiddleListener(OnAnimatorMiddleListener mOnAnimatorMiddleListener) {
        this.mOnAnimatorMiddleListener = mOnAnimatorMiddleListener;
        return this;
    }

    public MyValueAnimator setOnAnimatorUpdateListener(OnAnimatorUpdateListener listener) {
        mOnAnimatorUpdateListener = listener;
        return this;
    }

    private void update(float progress) {
        if (isAnimationStopped) {
            return;
        }
        float x = fromX + xPart * progress;
        float y = fromY + yPart * progress;
        if (mTargets != null) {
            for (Object tmp : mTargets) {
                if (tmp instanceof Pig) {
                    Pig pig = (Pig) tmp;
                    pig.setX(x);
                    pig.setY(y);
                } else if (tmp instanceof MyDrawable) {
                    MyDrawable myDrawable = (MyDrawable) tmp;
                    myDrawable.setX(x);
                    myDrawable.setY(y);
                }
            }
        }
    }

    public interface OnAnimatorUpdateListener {
        void onUpdate(float progress);
    }

    public interface OnAnimatorEndListener {
        void onAnimationEnd();
    }

    public interface OnAnimatorMiddleListener {
        void onAnimationMiddle();
    }
}
