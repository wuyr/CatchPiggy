package com.wuyr.catchpiggy.customize;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntDef;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.view.MotionEvent;

import com.wuyr.catchpiggy.R;
import com.wuyr.catchpiggy.models.WayData;
import com.wuyr.catchpiggy.utils.BitmapUtil;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by wuyr on 17-11-21 下午5:45.
 */

public class Pig {

    @IntDef({STATE_DRAGGING, STATE_RUNNING, STATE_STANDING})
    @IntRange(from = STATE_STANDING, to = STATE_DRAGGING)
    @Retention(RetentionPolicy.SOURCE)
    private @interface State {
    }

    @IntDef({ORIENTATION_LEFT, ORIENTATION_RIGHT})
    @IntRange(from = ORIENTATION_LEFT, to = ORIENTATION_RIGHT)
    @Retention(RetentionPolicy.SOURCE)
    private @interface Orientation {
    }

    public static final int STATE_STANDING = 0, STATE_RUNNING = 1, STATE_DRAGGING = 2;
    public static final int ORIENTATION_LEFT = 0, ORIENTATION_RIGHT = 1;
    private int mState, mOrientation;
    private int mIndex;
    private volatile int mVerticalPos, mHorizontalPos;
    private int mWidth, mHeight;
    private boolean isEnable;
    private float mScale;
    private float mX, mY;
    private MyDrawable mRunningLeftDrawable, mDraggingLeftDrawable, mRunningRightDrawable,
            mDraggingRightDrawable, mStandingLeftDrawable, mStandingRightDrawable;
    private OnTouchListener mOnTouchListener;
    private MyValueAnimator mValueAnimator;
    private PathAnimation mPathAnimation;
    private int mItemSize;
    private List<WayData> mPathData;
    private OnPositionUpdateListener mOnPositionUpdateListener;
    private OnLeavedListener mOnLeavedListener;
    private Semaphore mSemaphore;
    public volatile boolean isInitialized;
    public boolean isLeaved;

    public Pig(Context context, float scale) {
        mScale = scale;
        mSemaphore = new Semaphore(1);
        Bitmap bitmap = BitmapUtil.getBitmapFromResource(context, R.mipmap.ic_occupied_right_0);
        mWidth = (int) (bitmap.getWidth() * scale);
        mHeight = (int) (bitmap.getHeight() * scale);
        mStandingLeftDrawable = new MyDrawable(0, BitmapUtil.scaleBitmap(bitmap, mWidth, mHeight));
        mStandingRightDrawable = new MyDrawable(0, BitmapUtil.scaleBitmap(BitmapUtil.getBitmapFromResource(context, R.mipmap.ic_occupied_left_0), mWidth, mHeight));
        mRunningLeftDrawable = new MyDrawable(47, getRunningLeftBitmaps(context));
        mRunningRightDrawable = new MyDrawable(47, getRunningRightBitmaps(context));
        mDraggingLeftDrawable = new MyDrawable(70, getDraggingLeftBitmaps(context));
        mDraggingRightDrawable = new MyDrawable(70, getDraggingRightBitmaps(context));
        mRunningLeftDrawable.start();
        mRunningRightDrawable.start();
        mDraggingLeftDrawable.start();
        mDraggingRightDrawable.start();
        setState(STATE_STANDING);
    }

    public Drawable getCurrentDrawable() {
        Drawable drawable;
        switch (mState) {
            case STATE_STANDING:
                drawable = mOrientation == ORIENTATION_LEFT ? mStandingRightDrawable : mStandingLeftDrawable;
                break;
            case STATE_RUNNING:
                drawable = mOrientation == ORIENTATION_LEFT ? mRunningLeftDrawable : mRunningRightDrawable;
                break;
            case STATE_DRAGGING:
                drawable = mOrientation == ORIENTATION_LEFT ? mDraggingLeftDrawable : mDraggingRightDrawable;
                break;
            default:
                drawable = null;
        }
        return drawable;
    }

    public void onTouch(MotionEvent event, int index) {
        if (mOnTouchListener != null && isEnable) {
            mOnTouchListener.onTouch(this, event, index);
        }
    }

    public void setOnTouchListener(OnTouchListener onTouchListener) {
        mOnTouchListener = onTouchListener;
    }

    public void setOnLeavedListener(OnLeavedListener listener) {
        mOnLeavedListener = listener;
    }

    public void setOnPositionUpdateListener(OnPositionUpdateListener listener, int itemSize) {
        mOnPositionUpdateListener = listener;
        mItemSize = itemSize;
    }

    private WayData getPosition(float currentProgress) {
        float totalWayLength = mPathData.size() * mItemSize;
        int currentIndex = (int) (totalWayLength * currentProgress / mItemSize);
        try {
            mSemaphore.acquire();
        } catch (InterruptedException e) {
            return null;
        }
        WayData result = mPathData.get(currentIndex);
        mSemaphore.release();
        return result;
    }

    public void startTranslateAnimation(int toX, int toY) {
        mValueAnimator = MyValueAnimator.create(getX(), toX, getY(), toY, this).setDuration(150);
        mValueAnimator.start();
    }

    public void stopTranslateAnimation() {
        if (mValueAnimator != null) {
            mValueAnimator.stop();
            mValueAnimator = null;
        }
    }

    public PathAnimation setPathAnimation(MyPath path, List<WayData> pathData) {
        mPathData = pathData;
        if (mPathAnimation == null) {
            mPathAnimation = new PathAnimation(path);
        } else {
            mPathAnimation.updatePath(path);
        }
        if (mPathAnimation.getUpdateListener() == null) {
            mPathAnimation.setUpdateListener((currentProgress, pointF) -> {
                if (mOnPositionUpdateListener != null) {
                    WayData newPosition = getPosition(currentProgress);
                    if (newPosition != null && !(newPosition.x == mHorizontalPos && newPosition.y == mVerticalPos)) {
                        mOnPositionUpdateListener.onUpdate(this, new WayData(mHorizontalPos, mVerticalPos), newPosition);
                    }
                }
                setX(pointF.x, true);
                setY(pointF.y);
            });
        }
        if (mPathAnimation.getAnimationListener() == null) {
            mPathAnimation.setAnimationListener(new PathAnimation.AnimationListener() {
                @Override
                public void onAnimationStart() {
                    setState(Pig.STATE_RUNNING);
                }

                @Override
                public void onAnimationEnd() {
                    setState(Pig.STATE_STANDING);
                    if (!mPathAnimation.isAnimationRepeat()) {
                        setEnable(false);
                        isLeaved = true;
                        if (mOnLeavedListener != null) {
                            mOnLeavedListener.onLeaved();
                        }
                    }
                    mPathData = null;
                }

                @Override
                public void onAnimationCanceled() {
                    mPathData = null;
                }

                @Override
                public void onAnimationRepeat() {
                    try {
                        mSemaphore.acquire();
                    } catch (InterruptedException e) {
                        return;
                    }
                    Collections.reverse(mPathData);
                    mSemaphore.release();
                }
            });
        }
        return mPathAnimation;
    }

    public void startPathAnimation() {
        if (mPathAnimation != null) {
            mPathAnimation.start();
        }
    }

    public void cancelPathAnimation() {
        if (mPathAnimation != null) {
            mPathAnimation.cancel();
        }
    }

    private void stopPathAnimation() {
        if (mPathAnimation != null) {
            mPathAnimation.stop();
        }
    }

    public void release() {
        stopTranslateAnimation();
        stopPathAnimation();
        if (mRunningLeftDrawable != null) {
            mRunningLeftDrawable.release();
            mRunningLeftDrawable = null;
        }
        if (mDraggingLeftDrawable != null) {
            mDraggingLeftDrawable.release();
            mDraggingLeftDrawable = null;
        }
        if (mDraggingRightDrawable != null) {
            mDraggingRightDrawable.release();
            mDraggingRightDrawable = null;
        }
        if (mRunningRightDrawable != null) {
            mRunningRightDrawable.release();
            mRunningRightDrawable = null;
        }
        if (mStandingLeftDrawable != null) {
            mStandingLeftDrawable.release();
            mStandingLeftDrawable = null;
        }
        if (mStandingRightDrawable != null) {
            mStandingRightDrawable.release();
            mStandingRightDrawable = null;
        }
        mOnTouchListener = null;
        mPathAnimation = null;
        mPathData = null;
        mOnPositionUpdateListener = null;
    }

    public interface OnTouchListener {
        void onTouch(Pig pig, MotionEvent event, int index);
    }

    public void setEnable(boolean enable) {
        isEnable = enable;
    }

    public int getWidth() {
        return mRunningLeftDrawable.getIntrinsicWidth();
    }

    public int getDragWidth() {
        return mDraggingLeftDrawable.getIntrinsicWidth();
    }

    public int getHeight() {
        return mRunningLeftDrawable.getIntrinsicHeight();
    }

    public int getState() {
        return mState;
    }

    public void setState(@State int state) {
        mState = state;
        switch (state) {
            case STATE_STANDING:
                mRunningLeftDrawable.pause();
                mRunningRightDrawable.pause();
                mDraggingLeftDrawable.pause();
                mDraggingRightDrawable.pause();
                break;
            case STATE_RUNNING:
                mDraggingLeftDrawable.pause();
                mDraggingRightDrawable.pause();
                if (mOrientation == ORIENTATION_LEFT) {
                    mRunningRightDrawable.pause();
                    mRunningLeftDrawable.resume();
                } else {
                    mRunningLeftDrawable.pause();
                    mRunningRightDrawable.resume();
                }
                break;
            case STATE_DRAGGING:
                mRunningLeftDrawable.pause();
                mRunningRightDrawable.pause();
                if (mOrientation == ORIENTATION_LEFT) {
                    mDraggingRightDrawable.pause();
                    mDraggingLeftDrawable.resume();
                } else {
                    mDraggingLeftDrawable.pause();
                    mDraggingRightDrawable.resume();
                }
                break;
            default:
                break;
        }
    }

    public List<WayData> getPathData() {
        return mPathData == null ? new ArrayList<>() : mPathData;
    }

    public boolean isRepeatAnimation() {
        return mPathAnimation != null && mPathAnimation.isAnimationRepeat();
    }

    public int getOrientation() {
        return mOrientation;
    }

    private void setOrientation(@Orientation int orientation) {
        mOrientation = orientation;
        setState(mState);
    }

    public int getIndex() {
        return mIndex;
    }

    public void setIndex(int index) {
        mIndex = index;
    }

    public float getX() {
        return mX;
    }

    public void setX(float x) {
        setX(x, false);
    }

    private void setX(float x, boolean isProgressUpdate) {
        if (isProgressUpdate) {
            if (x > mX && mOrientation != ORIENTATION_RIGHT) {
                setOrientation(ORIENTATION_RIGHT);
            } else if (x < mX && mOrientation != ORIENTATION_LEFT) {
                setOrientation(ORIENTATION_LEFT);
            }
        }
        mX = x;
        mRunningLeftDrawable.setX(x);
        mRunningRightDrawable.setX(x);
        mDraggingLeftDrawable.setX(x);
        mDraggingRightDrawable.setX(x);
        mStandingLeftDrawable.setX(x);
        mStandingRightDrawable.setX(x);
    }

    public void setY(float y) {
        mY = y;
        mRunningLeftDrawable.setY(y);
        mRunningRightDrawable.setY(y);
        mDraggingLeftDrawable.setY(y);
        mDraggingRightDrawable.setY(y);
        mStandingLeftDrawable.setY(y);
        mStandingRightDrawable.setY(y);
    }

    public float getY() {
        return mY;
    }

    public WayData getPosition() {
        AtomicReference<WayData> point = new AtomicReference<>(new WayData(mHorizontalPos, mVerticalPos));
        return point.get();
    }

    public void setPosition(int verticalPos, int horizontalPos) {
        mVerticalPos = verticalPos;
        mHorizontalPos = horizontalPos;
    }

    private Bitmap[] getDraggingRightBitmaps(Context context) {
        Bitmap bitmap1 = BitmapUtil.getBitmapFromResource(context, R.mipmap.ic_drop_right_1);
        int height = (int) (bitmap1.getHeight() * mScale);
        int width = (int) (bitmap1.getWidth() * mScale);
        bitmap1 = BitmapUtil.scaleBitmap(bitmap1, width, height);
        return new Bitmap[]{
                BitmapUtil.scaleBitmap(BitmapUtil.getBitmapFromResource(context, R.mipmap.ic_drop_right_0), width, height),
                bitmap1,
                BitmapUtil.scaleBitmap(BitmapUtil.getBitmapFromResource(context, R.mipmap.ic_drop_right_2), width, height),
                bitmap1};
    }

    private Bitmap[] getDraggingLeftBitmaps(Context context) {
        Bitmap bitmap1 = BitmapUtil.getBitmapFromResource(context, R.mipmap.ic_drop_left_1);
        int height = (int) (bitmap1.getHeight() * mScale);
        int width = (int) (bitmap1.getWidth() * mScale);
        bitmap1 = BitmapUtil.scaleBitmap(bitmap1, width, height);
        return new Bitmap[]{
                BitmapUtil.scaleBitmap(BitmapUtil.getBitmapFromResource(context, R.mipmap.ic_drop_left_0), width, height),
                bitmap1,
                BitmapUtil.scaleBitmap(BitmapUtil.getBitmapFromResource(context, R.mipmap.ic_drop_left_2), width, height),
                bitmap1};
    }

    private Bitmap[] getRunningRightBitmaps(Context context) {
        return new Bitmap[]{
                BitmapUtil.scaleBitmap(BitmapUtil.getBitmapFromResource(context, R.mipmap.ic_occupied_right_1), mWidth, mHeight),
                BitmapUtil.scaleBitmap(BitmapUtil.getBitmapFromResource(context, R.mipmap.ic_occupied_right_2), mWidth, mHeight),
                BitmapUtil.scaleBitmap(BitmapUtil.getBitmapFromResource(context, R.mipmap.ic_occupied_right_3), mWidth, mHeight),
                BitmapUtil.scaleBitmap(BitmapUtil.getBitmapFromResource(context, R.mipmap.ic_occupied_right_4), mWidth, mHeight),
                BitmapUtil.scaleBitmap(BitmapUtil.getBitmapFromResource(context, R.mipmap.ic_occupied_right_5), mWidth, mHeight),
                BitmapUtil.scaleBitmap(BitmapUtil.getBitmapFromResource(context, R.mipmap.ic_occupied_right_6), mWidth, mHeight),
                BitmapUtil.scaleBitmap(BitmapUtil.getBitmapFromResource(context, R.mipmap.ic_occupied_right_7), mWidth, mHeight),
                BitmapUtil.scaleBitmap(BitmapUtil.getBitmapFromResource(context, R.mipmap.ic_occupied_right_8), mWidth, mHeight)};
    }

    @NonNull
    private Bitmap[] getRunningLeftBitmaps(Context context) {
        return new Bitmap[]{
                BitmapUtil.scaleBitmap(BitmapUtil.getBitmapFromResource(context, R.mipmap.ic_occupied_left_1), mWidth, mHeight),
                BitmapUtil.scaleBitmap(BitmapUtil.getBitmapFromResource(context, R.mipmap.ic_occupied_left_2), mWidth, mHeight),
                BitmapUtil.scaleBitmap(BitmapUtil.getBitmapFromResource(context, R.mipmap.ic_occupied_left_3), mWidth, mHeight),
                BitmapUtil.scaleBitmap(BitmapUtil.getBitmapFromResource(context, R.mipmap.ic_occupied_left_4), mWidth, mHeight),
                BitmapUtil.scaleBitmap(BitmapUtil.getBitmapFromResource(context, R.mipmap.ic_occupied_left_5), mWidth, mHeight),
                BitmapUtil.scaleBitmap(BitmapUtil.getBitmapFromResource(context, R.mipmap.ic_occupied_left_6), mWidth, mHeight),
                BitmapUtil.scaleBitmap(BitmapUtil.getBitmapFromResource(context, R.mipmap.ic_occupied_left_7), mWidth, mHeight),
                BitmapUtil.scaleBitmap(BitmapUtil.getBitmapFromResource(context, R.mipmap.ic_occupied_left_8), mWidth, mHeight)};
    }

    public interface OnPositionUpdateListener {
        void onUpdate(Pig pig, WayData oldPosition, WayData newPosition);
    }

    public interface OnLeavedListener {
        void onLeaved();
    }
}
