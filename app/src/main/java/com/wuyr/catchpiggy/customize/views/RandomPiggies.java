package com.wuyr.catchpiggy.customize.views;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.ImageView;

import com.wuyr.catchpiggy.R;
import com.wuyr.catchpiggy.customize.MyLayoutParams;
import com.wuyr.catchpiggy.models.WayData;
import com.wuyr.catchpiggy.utils.ThreadPool;
import com.wuyr.catchpiggy.utils.ValueAnimatorUtil;

import java.util.Random;
import java.util.concurrent.Future;

/**
 * Created by wuyr on 17-10-30 上午12:43.
 */

/**
 * 主页中生成随机小猪跑过的动画
 */
public class RandomPiggies extends ViewGroup {

    private Random mRandom;
    private Future mTask;//生成小猪动画的线程
    private volatile boolean isNeed;//需要生成
    private OnTouchListener mOnTouchListener;
    private int mDropWidth, mDropHeight,//小猪接受触摸事件的长宽
            mRunWidth;//小猪实际宽度

    public RandomPiggies(Context context) {
        this(context, null);
    }

    public RandomPiggies(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RandomPiggies(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void init() {
        mRandom = new Random();
        ImageView imageView = new ImageView(getContext());
        imageView.setImageResource(R.drawable.anim_drop_left);
        AnimationDrawable drawable = (AnimationDrawable) imageView.getDrawable();
        mDropWidth = drawable.getIntrinsicWidth();
        mDropHeight = drawable.getIntrinsicHeight();

        mOnTouchListener = (v, event) -> {
            int x = (int) (event.getX() + v.getLeft() + v.getTranslationX());
            int y = (int) event.getY() + v.getTop();
            MyLayoutParams lp = (MyLayoutParams) v.getLayoutParams();
            switch (event.getAction()) {
                //手指按住小猪,就播放被拖动的动画
                case MotionEvent.ACTION_DOWN:
                    ((ViewPropertyAnimator) v.getTag()).cancel();
                    ((ImageView) v).setImageResource(lp.isLeft ? R.drawable.anim_drop_right : R.drawable.anim_drop_left);
                    AnimationDrawable drawable1 = (AnimationDrawable) ((ImageView) v).getDrawable();
                    drawable1.start();
                    MyLayoutParams layoutParams = new MyLayoutParams((int) (mDropWidth * lp.scale), (int) (mDropHeight * lp.scale));
                    layoutParams.x = (int) (v.getX() - (mDropWidth - mRunWidth));
                    layoutParams.y = (int) v.getY();
                    lp.isDrag = true;
                    layoutParams.scale = lp.scale;
                    layoutParams.isLeft = lp.isLeft;
                    v.setLayoutParams(layoutParams);
                    v.setTag(R.id.position_data, new WayData(x, y));
                    requestLayout();
                    break;
                //跟随手指移动
                case MotionEvent.ACTION_MOVE:
                    lp.isDrag = true;
                    int lastX = 0, lastY = 0;
                    WayData data = (WayData) v.getTag(R.id.position_data);
                    if (data != null) {
                        lastX = data.x;
                        lastY = data.y;
                    }
                    lp.x = v.getLeft() + (x - lastX);
                    lp.y = v.getTop() + (y - lastY);
                    requestLayout();
                    v.setTag(R.id.position_data, new WayData(x, y));
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    //手指松开后,恢复播放动画
                    //根据 时间 * 速度 = 路程
                    //先计算出小猪的速度(因为每个小猪的速度都是随机的)
                    //然后根据速度和剩下的距离计算出需要的时长
                    lp.isDrag = true;
                    lastX = 0;
                    lastY = 0;
                    data = (WayData) v.getTag(R.id.position_data);
                    if (data != null) {
                        lastX = data.x;
                        lastY = data.y;
                    }
                    lp.x = v.getLeft() + (x - lastX);
                    lp.y = v.getTop() + (y - lastY);
                    requestLayout();
                    v.setTag(R.id.position_data, new WayData(x, y));
                    lp.isDrag = false;
                    ((ImageView) v).setImageResource(lp.isLeft ? R.drawable.anim_run_right2 : R.drawable.anim_run_left2);
                    drawable1 = (AnimationDrawable) ((ImageView) v).getDrawable();
                    drawable1.start();
                    v.setTranslationX(lp.isLeft ? v.getWidth() + v.getTranslationX() + lp.x :
                            -(getWidth() + v.getWidth()) + v.getWidth() + v.getTranslationX() + lp.x);
                    ViewPropertyAnimator animator = v.animate();
                    float oldSpeed = (float) (getWidth() + v.getWidth()) / ((long) v.getTag(R.id.current_duration));
                    float distance = Math.abs(lp.isLeft ? (getWidth() + v.getWidth()) - v.getTranslationX() : (-(getWidth() + v.getWidth())) - v.getTranslationX());
                    float newDuration = distance / oldSpeed;
                    animator.translationX(lp.isLeft ? getWidth() + v.getWidth() : -(getWidth() + v.getWidth()))
                            .setDuration((long) newDuration).setListener(new AnimatorListener(this, v));
                    v.setTag(animator);
                    ValueAnimatorUtil.resetDurationScale();
                    animator.start();
                    break;
            }
            return true;
        };
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            MyLayoutParams lp = (MyLayoutParams) view.getLayoutParams();
            if (lp.isDrag) {
                view.layout(lp.x, lp.y, lp.x + lp.width, lp.y + lp.height);
            } else {
                view.layout(lp.isLeft ? -lp.width : getWidth(),
                        lp.y, lp.isLeft ? 0 : getWidth() + lp.width, lp.y + lp.height);
            }
        }
    }

    public void startShow() {
        //避免多个线程同时生成
        if (mTask != null) {
            stopShow();
        }
        mTask = ThreadPool.getInstance().execute(() -> {
            isNeed = true;
            while (isNeed) {
                //生成随机大小,方向,速度的小猪跑过的动画,并接受触摸事件
                post(() -> {
                    ImageView imageView = new ImageView(getContext());
                    MyLayoutParams layoutParams = new MyLayoutParams(0, 0);
                    boolean isLeft = mRandom.nextBoolean();
                    float scale = mRandom.nextFloat() + .5F;
                    if (scale > 1) {
                        scale = 1;
                    }
                    imageView.setImageResource(isLeft ? R.drawable.anim_run_right2 : R.drawable.anim_run_left2);
                    AnimationDrawable drawable = (AnimationDrawable) imageView.getDrawable();
                    drawable.start();
                    if (mRunWidth == 0) {
                        mRunWidth = drawable.getIntrinsicWidth();
                    }
                    layoutParams.width = (int) (drawable.getIntrinsicWidth() * scale);
                    layoutParams.height = (int) (drawable.getIntrinsicHeight() * scale);
                    if (getHeight() <= 0) {
                        stopShow();
                        return;
                    }
                    layoutParams.y = mRandom.nextInt(getHeight() - drawable.getIntrinsicHeight());
                    layoutParams.isLeft = isLeft;
                    layoutParams.scale = scale;
                    imageView.setLayoutParams(layoutParams);
                    addView(imageView);
                    imageView.setOnTouchListener(mOnTouchListener);

                    ViewPropertyAnimator animator = imageView.animate();
                    animator.translationX(isLeft ? getWidth() + layoutParams.width : -(getWidth() + layoutParams.width))
                            .setDuration(1250 + mRandom.nextInt(2750)).setListener(new AnimatorListener(RandomPiggies.this, imageView));
                    imageView.setTag(R.id.current_duration, animator.getDuration());
                    imageView.setTag(animator);
                    ValueAnimatorUtil.resetDurationScale();
                    animator.start();
                });
                try {
                    Thread.sleep(300 + mRandom.nextInt(1700));
                } catch (InterruptedException e) {
                    return;
                }
            }
        });
    }

    public void stopShow() {
        isNeed = false;
        if (mTask != null) {
            mTask.cancel(true);
            mTask = null;
        }
    }

    @Override
    protected MyLayoutParams generateDefaultLayoutParams() {
        return new MyLayoutParams(0, 0);
    }

    private static class AnimatorListener implements Animator.AnimatorListener {

        private ViewGroup mViewGroup;
        private View mView;
        private boolean isHasDragAction;

        AnimatorListener(ViewGroup viewGroup, View view) {
            mViewGroup = viewGroup;
            mView = view;
        }

        @Override
        public void onAnimationStart(Animator animation) {
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            //小猪跑出屏幕后自动移除
            if (!isHasDragAction) {
                mViewGroup.removeView(mView);
                mViewGroup = null;
                mView = null;
            }
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            isHasDragAction = true;
        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    }
}
