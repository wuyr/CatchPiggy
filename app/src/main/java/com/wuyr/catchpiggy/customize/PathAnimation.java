package com.wuyr.catchpiggy.customize;

import android.graphics.PointF;
import android.os.Build;
import android.os.SystemClock;

import com.wuyr.catchpiggy.utils.ThreadPool;

/**
 * Created by wuyr on 17-11-22 下午5:02.
 */

public class PathAnimation {

    private Keyframes mPathKeyframes;
    private long mAnimationDuration;
    private OnAnimationUpdateListener mOnAnimationUpdateListener;
    private AnimationListener mAnimationListener;
    private volatile boolean isAnimationRepeat, isAnimationStopped, isAnimationCanceled, isAnimationEndListenerCalled;

    PathAnimation(MyPath path) {
        updatePath(path);
    }

    public PathAnimation setDuration(long duration) {
        mAnimationDuration = duration;
        return this;
    }

    void updatePath(MyPath path) {
        mPathKeyframes = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? new PathKeyframes(path) : new PathKeyframesSupport(path);
    }

    void setUpdateListener(OnAnimationUpdateListener listener) {
        mOnAnimationUpdateListener = listener;
    }

    OnAnimationUpdateListener getUpdateListener() {
        return mOnAnimationUpdateListener;
    }

    public PathAnimation setRepeat(boolean isAnimationRepeat) {
        this.isAnimationRepeat = isAnimationRepeat;
        return this;
    }

    boolean isAnimationRepeat() {
        return isAnimationRepeat;
    }

    void setAnimationListener(AnimationListener listener) {
        mAnimationListener = listener;
    }

    AnimationListener getAnimationListener() {
        return mAnimationListener;
    }

    void start() {
        if (mAnimationDuration > 0) {
            ThreadPool.getInstance().execute(() -> {
                isAnimationStopped = false;
                isAnimationCanceled = false;
                isAnimationEndListenerCalled = false;
                final long startTime = SystemClock.uptimeMillis();
                long currentPlayedDuration;
                if (mAnimationListener != null) {
                    mAnimationListener.onAnimationStart();
                }
                while ((currentPlayedDuration = SystemClock.uptimeMillis() - startTime) < mAnimationDuration) {
                    if (isAnimationInterrupted()) {
                        break;
                    }
                    float progress = (float) currentPlayedDuration / (float) mAnimationDuration;
                    if (mOnAnimationUpdateListener != null) {
                        if (!isAnimationInterrupted()) {
                            mOnAnimationUpdateListener.onUpdate(progress, mPathKeyframes.getValue(progress));
                        }
                    }
                }
                if (isAnimationRepeat && !isAnimationInterrupted()) {
                    mPathKeyframes.reverse();
                    if (mAnimationListener != null) {
                        mAnimationListener.onAnimationRepeat();
                    }
                    start();
                } else {
                    isAnimationStopped = true;
                    if (mAnimationListener != null) {
                        if (isAnimationCanceled) {
                            mAnimationListener.onAnimationCanceled();
                        } else {
                            mAnimationListener.onAnimationEnd();
                        }
                    }
                    isAnimationEndListenerCalled = true;
                }
            });
        }
    }

    private void waitStopped() {
        isAnimationStopped = true;
        //noinspection StatementWithEmptyBody
        while (!isAnimationEndListenerCalled) {
        }
    }

    private void waitCancel(){
        isAnimationCanceled = true;
        //noinspection StatementWithEmptyBody
        while (!isAnimationEndListenerCalled) {
        }
    }

    void stop() {
        waitStopped();
    }

    void cancel() {
        waitCancel();
    }

    private boolean isAnimationInterrupted() {
        return isAnimationCanceled || isAnimationStopped;
    }

    public interface OnAnimationUpdateListener {
        void onUpdate(float currentProgress, PointF position);
    }

    public interface AnimationListener {
        void onAnimationStart();

        void onAnimationEnd();

        void onAnimationCanceled();

        void onAnimationRepeat();
    }
}
