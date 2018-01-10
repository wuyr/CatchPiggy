package com.wuyr.catchpiggy.customize.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.wuyr.catchpiggy.R;
import com.wuyr.catchpiggy.customize.MyValueAnimator;
import com.wuyr.catchpiggy.utils.ThreadPool;

/**
 * Created by wuyr on 17-12-24 下午9:51.
 */

/**
 * 界面之间跳转的过度动画
 */
public class LoadingView extends SurfaceView implements Runnable {

    public boolean isOpen, isLoading, isProcessing;
    private Paint mPaint;
    //中心点的坐标和当前圆的半径
    private int mCenterX, mCenterY, mCurrentRadius;
    private int mMaxRadius;//圆的半径
    private SurfaceHolder mSurfaceHolder;

    public LoadingView(Context context) {
        this(context, null);
    }

    public LoadingView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint();
        mPaint.setColor(getResources().getColor(R.color.colorLoadingViewBackground));
        mPaint.setAntiAlias(true);
        setVisibility(INVISIBLE);
        setZOrderOnTop(true);
        mSurfaceHolder = getHolder();
        mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
    }

    public void startLoad(OnAnimationFinishListener listener) {
        if (listener == null) {
            return;
        }
        isLoading = isOpen = isProcessing = true;
        startAnimation(() -> post(() -> {
            listener.onAnimationFinish();
            finishLoad();
        }));
    }

    private void finishLoad() {
        isOpen = false;
        startAnimation(() -> post(() -> {
            setVisibility(INVISIBLE);
            isLoading = isProcessing = false;
        }));
    }

    /**
     * 播放动画
     */
    private void startAnimation(MyValueAnimator.OnAnimatorEndListener onAnimatorEndListener) {
        setVisibility(VISIBLE);
        ThreadPool.getInstance().execute(this);
        MyValueAnimator.create(0, 0, 0, 0).setDuration(350L).setOnAnimatorUpdateListener(progress -> {
            if (!isOpen) {
                progress = 1F - progress;
            }
            //更新圆的半径
            mCurrentRadius = (int) (mMaxRadius * progress);
        }).setOnAnimatorEndListener(onAnimatorEndListener).start();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mCenterX = w / 2;
        mCenterY = h / 2;
        //圆的最大半径取手机屏幕对角线的一半
        mMaxRadius = (int) Math.sqrt(Math.pow(mCenterX, 2) + Math.pow(mCenterY, 2));
    }

    @Override
    public void run() {
        while (isProcessing) {
            Canvas canvas = mSurfaceHolder.lockCanvas();
            if (canvas == null) {
                return;
            }
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            //画圆
            canvas.drawCircle(mCenterX, mCenterY, mCurrentRadius, mPaint);
            mSurfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    public interface OnAnimationFinishListener {
        void onAnimationFinish();
    }
}
