package com.wuyr.catchpiggy.customize;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import com.wuyr.catchpiggy.utils.ThreadPool;

import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

/**
 * Created by wuyr on 17-11-21 下午8:03.
 */

/**
 * 自定义的Drawable，类似于AnimationDrawable
 */
public class MyDrawable extends Drawable implements Cloneable {

    private final int mDelay;//帧延时
    private final byte[] mLock;//控制线程暂停的锁
    private Semaphore mSemaphore;//来用控制线程更新问题
    private Bitmap[] mBitmaps;//帧
    private Paint mPaint;
    private int mCurrentIndex;//当前帧索引
    private float x, y;//当前坐标
    private Future mTask;//帧动画播放的任务
    private volatile boolean isPaused;//已暂停

    public MyDrawable(int delay, Bitmap... bitmaps) {
        mSemaphore = new Semaphore(1);
        mBitmaps = bitmaps;
        mDelay = delay;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mLock = new byte[0];
    }

    public void start() {
        stop();
        mTask = ThreadPool.getInstance().execute(() -> {
            while (true) {
                synchronized (mLock) {
                    while (isPaused) {
                        try {
                            mLock.wait();
                        } catch (InterruptedException e) {
                            return;
                        }
                    }
                }
                try {
                    Thread.sleep(mDelay);
                } catch (InterruptedException e) {
                    return;
                }
                try {
                    mSemaphore.acquire();
                } catch (InterruptedException e) {
                    return;
                }
                mCurrentIndex++;
                if (mCurrentIndex == mBitmaps.length) {
                    mCurrentIndex = 0;
                }
                mSemaphore.release();
            }
        });
    }

    void pause() {
        isPaused = true;
    }

    void resume() {
        isPaused = false;
        synchronized (mLock) {
            mLock.notifyAll();
        }
    }

    private void stop() {
        if (mTask != null) {
            mTask.cancel(true);
            mTask = null;
            mCurrentIndex = 0;
        }
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        try {
            mSemaphore.acquire();
        } catch (InterruptedException e) {
            return;
        }
        canvas.drawBitmap(mBitmaps[mCurrentIndex], x, y, mPaint);
        mSemaphore.release();
    }

    public void release() {
        stop();
        if (mBitmaps != null) {
            for (Bitmap bitmap : mBitmaps) {
                if (bitmap != null && !bitmap.isRecycled()) {
                    bitmap.recycle();
                }
            }
        }
        mBitmaps = null;
        mPaint = null;
        mTask = null;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public Bitmap getBitmap() {
        Bitmap result = null;
        if (mBitmaps != null && mBitmaps.length > 0) {
            result = mBitmaps[0];
        }
        return result;
    }

    @Override
    public int getIntrinsicWidth() {
        if (mBitmaps.length == 0) {
            return 0;
        }
        return mBitmaps[0].getWidth();
    }

    @Override
    public int getIntrinsicHeight() {
        if (mBitmaps.length == 0) {
            return 0;
        }
        return mBitmaps[0].getHeight();
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mPaint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public MyDrawable clone() {
        return new MyDrawable(0, mBitmaps[0]);
    }
}
