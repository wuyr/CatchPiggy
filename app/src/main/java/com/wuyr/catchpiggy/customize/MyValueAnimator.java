package com.wuyr.catchpiggy.customize;

import android.os.SystemClock;

import com.wuyr.catchpiggy.utils.ThreadPool;

/**
 * Created by wuyr on 17-11-27 上午1:57.
 */

/**
 * 自定义的属性动画类(类似于ValueAnimator)
 */
public class MyValueAnimator {
    private long duration;
    private Object[] mTargets;//参与改变属性的对象
    private OnAnimatorEndListener mOnAnimatorEndListener;//动画播放完
    private OnAnimatorMiddleListener mOnAnimatorMiddleListener;//动画播放到一半
    private volatile boolean isAnimationStopped;//已停止
    private float fromX, fromY, xPart, yPart;
    private boolean isRunOnCurrentThread, isOnAnimatorMiddleListenerCalled;//动画播放到一半的接口已回调
    private OnAnimatorUpdateListener mOnAnimatorUpdateListener;//动画更新回调

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
            //是本线程动画,就直接开始,否则另开新线程
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
        long currentPlayedDuration;//当前动画已经播放的时长
        while ((currentPlayedDuration = SystemClock.uptimeMillis() - startTime) < duration) {
            if (isAnimationStopped) {
                break;
            }
            //根据当前动画已经播放的时长和总动画时长计算出当前动画的播放进度
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

    /**
     * 在当前线程中执行动画
     */
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

    /**
     更新对象的x,y值
     */
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
