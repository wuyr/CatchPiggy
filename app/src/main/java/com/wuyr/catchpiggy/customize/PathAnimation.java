package com.wuyr.catchpiggy.customize;

import android.graphics.PointF;
import android.os.Build;
import android.os.SystemClock;

import com.wuyr.catchpiggy.utils.ThreadPool;

/**
 * Created by wuyr on 17-11-22 下午5:02.
 */

/**
 * 自己封装的路径动画类
 */
public class PathAnimation {

    private Keyframes mPathKeyframes;//关键帧
    private long mAnimationDuration;//动画时长
    private OnAnimationUpdateListener mOnAnimationUpdateListener;//动画更新监听
    private AnimationListener mAnimationListener;//动画事件监听
    private volatile boolean isAnimationRepeat, //反复播放的动画
            isAnimationStopped,//已停止
            isAnimationCanceled, //已取消 (停止和取消的区别: 取消是在动画播放完之前主动取消的,  停止是动画播放完,自动停止的)
            isAnimationEndListenerCalled;//动画已取消的监听已经回调

    PathAnimation(MyPath path) {
        updatePath(path);
    }

    public PathAnimation setDuration(long duration) {
        mAnimationDuration = duration;
        return this;
    }

    void updatePath(MyPath path) {
        //根据系统版本选择更合适的关键帧类
        mPathKeyframes = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? new PathKeyframes(path) : new PathKeyframesSupport(path);
    }

    OnAnimationUpdateListener getUpdateListener() {
        return mOnAnimationUpdateListener;
    }

    void setUpdateListener(OnAnimationUpdateListener listener) {
        mOnAnimationUpdateListener = listener;
    }

    /**
     * 设置动画是否重复播放
     */
    public PathAnimation setRepeat(boolean isAnimationRepeat) {
        this.isAnimationRepeat = isAnimationRepeat;
        return this;
    }

    boolean isAnimationRepeat() {
        return isAnimationRepeat;
    }

    AnimationListener getAnimationListener() {
        return mAnimationListener;
    }

    void setAnimationListener(AnimationListener listener) {
        mAnimationListener = listener;
    }

    void start() {
        if (mAnimationDuration > 0) {
            ThreadPool.getInstance().execute(() -> {
                isAnimationStopped = false;
                isAnimationCanceled = false;
                isAnimationEndListenerCalled = false;
                final long startTime = SystemClock.uptimeMillis();
                long currentPlayedDuration;//当前动画已经播放的时长
                if (mAnimationListener != null) {
                    mAnimationListener.onAnimationStart();
                }
                while ((currentPlayedDuration = SystemClock.uptimeMillis() - startTime) < mAnimationDuration) {
                    //如果动画被打断则跳出循环
                    if (isAnimationInterrupted()) {
                        break;
                    }
                    //根据当前动画已经播放的时长和总动画时长计算出当前动画的播放进度
                    float progress = (float) currentPlayedDuration / (float) mAnimationDuration;
                    if (mOnAnimationUpdateListener != null) {
                        if (!isAnimationInterrupted()) {
                            mOnAnimationUpdateListener.onUpdate(progress, mPathKeyframes.getValue(progress));
                        }
                    }
                }
                if (isAnimationRepeat && !isAnimationInterrupted()) {
                    //如果是设置了重复并且还没有被取消,则重复播放动画
                    mPathKeyframes.reverse();
                    if (mAnimationListener != null) {
                        mAnimationListener.onAnimationRepeat();
                    }
                    start();
                } else {
                    isAnimationStopped = true;
                    if (mAnimationListener != null) {
                        //判断应该回调哪一个接口
                        if (isAnimationCanceled) {
                            mAnimationListener.onAnimationCanceled();
                        } else {
                            mAnimationListener.onAnimationEnd();
                        }
                    }
                    //标记接口已回调
                    isAnimationEndListenerCalled = true;
                }
            });
        }
    }

    /**
     会阻塞,直到动画真正停止才返回
     */
    private void waitStopped() {
        isAnimationStopped = true;
        //noinspection StatementWithEmptyBody
        while (!isAnimationEndListenerCalled) {
        }
    }

    /**
     会阻塞,直到动画真正取消才返回
     */
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

    /**
     动画被打断
     */
    private boolean isAnimationInterrupted() {
        return isAnimationCanceled || isAnimationStopped;
    }

    public interface OnAnimationUpdateListener {
        void onUpdate(float currentProgress, PointF position);
    }

    public interface AnimationListener {
        void onAnimationStart();//动画开始

        void onAnimationEnd();//动画结束

        void onAnimationCanceled();//动画取消

        void onAnimationRepeat();//动画重复播放
    }
}
