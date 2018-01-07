package com.wuyr.catchpiggy.customize.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.IntDef;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by wuyr on 17-9-29 下午7:47.
 */

public class Item extends View {
    @IntDef({STATE_UNSELECTED, STATE_OCCUPIED, STATE_SELECTED, STATE_GUIDE, STATE_EMPTY})
    @IntRange(from = STATE_UNSELECTED, to = STATE_GUIDE)
    @Retention(RetentionPolicy.SOURCE)
    private @interface STATE {
    }

    public static final int STATE_UNSELECTED = 0, STATE_OCCUPIED = 1, STATE_SELECTED = 2, STATE_GUIDE = 4, STATE_EMPTY = 5;
    private int mCurrentStatus;
    private Paint mPaint;
    private OnItemPressedListener mOnItemPressedListener;
    private int mHorizontalPos, mVerticalPos;
    private Bitmap mUnSelectedBitmap, mSelectedBitmap, mOccupiedBitmapLeft, mOccupiedBitmapRight, mGuideBitmap;
    private boolean isLeft;
    private boolean isShowOccupiedImage, isShowSelectedImage;

    public Item(Context context) {
        this(context, null);
    }

    public Item(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Item(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        isShowSelectedImage = true;
    }

    public void setStatus(@STATE int status) {
        mCurrentStatus = status;
        isShowOccupiedImage = true;
        invalidate();
    }

    public int getStatus() {
        return mCurrentStatus;
    }

    public void setOnItemPressedListener(OnItemPressedListener listener) {
        mOnItemPressedListener = listener;
    }

    public void setPositions(int horizontalPos, int verticalPos) {
        this.mHorizontalPos = horizontalPos;
        this.mVerticalPos = verticalPos;
    }

    public void setIsLeft(boolean isLeft) {
        this.isLeft = isLeft;
    }

    public boolean isLeft() {
        return isLeft;
    }

    public void setSelectedBitmap(Bitmap bitmap) {
        mSelectedBitmap = bitmap;
    }

    public void setUnSelectedBitmap(Bitmap bitmap) {
        mUnSelectedBitmap = bitmap;
    }

    public void setOccupiedBitmapLeft(Bitmap bitmap) {
        mOccupiedBitmapLeft = bitmap;
    }

    public void setOccupiedBitmapRight(Bitmap bitmap) {
        mOccupiedBitmapRight = bitmap;
    }

    public void setGuideBitmap(Bitmap bitmap){
        mGuideBitmap = bitmap;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mCurrentStatus == STATE_EMPTY) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mCurrentStatus == STATE_UNSELECTED || mCurrentStatus == STATE_GUIDE) {
                    //setStatus(STATE_SELECTED);
                    if (mOnItemPressedListener != null) {
                        mOnItemPressedListener.onPressed(mHorizontalPos, mVerticalPos);
                    }
                }
                break;
        }
        return true;
    }

    public void hideSelectedImage() {
        isShowSelectedImage = false;
    }

    public void showSelectedImage() {
        isShowSelectedImage = true;
        invalidate();
    }

    public void hideOccupiedImage() {
        isShowOccupiedImage = false;
        invalidate();
    }

    public void showOccupiedImage() {
        isShowOccupiedImage = true;
        invalidate();
    }

    public void release() {
        mPaint = null;
        mOnItemPressedListener = null;
        if (mUnSelectedBitmap!=null) {
            mUnSelectedBitmap.recycle();
            mUnSelectedBitmap = null;
        }
        if (mSelectedBitmap != null) {
            mSelectedBitmap.recycle();
            mSelectedBitmap = null;
        }
        if (mOccupiedBitmapLeft != null) {
            mOccupiedBitmapLeft.recycle();
            mOccupiedBitmapLeft = null;
        }
        if (mOccupiedBitmapRight != null) {
            mOccupiedBitmapRight.recycle();
            mOccupiedBitmapRight = null;
        }
        if (mGuideBitmap != null) {
            mGuideBitmap.recycle();
            mGuideBitmap = null;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        switch (mCurrentStatus) {
            case STATE_GUIDE:
                if (mGuideBitmap != null){
                    canvas.drawBitmap(mGuideBitmap, 0, getHeight() - mGuideBitmap.getHeight(), mPaint);
                }
                break;
            case STATE_UNSELECTED:
                if (mUnSelectedBitmap != null) {
                    canvas.drawBitmap(mUnSelectedBitmap, 0, getHeight() - mUnSelectedBitmap.getHeight(), mPaint);
                }
                break;
            case STATE_SELECTED:
                if (mUnSelectedBitmap != null) {
                    canvas.drawBitmap(mUnSelectedBitmap, 0, getHeight() - mUnSelectedBitmap.getHeight(), mPaint);
                }
                if (isShowSelectedImage && mSelectedBitmap != null) {
                    canvas.drawBitmap(mSelectedBitmap, 0, getHeight() - mSelectedBitmap.getHeight(), mPaint);
                }
                break;
            case STATE_OCCUPIED:
                if (mUnSelectedBitmap != null) {
                    canvas.drawBitmap(mUnSelectedBitmap, 0, getHeight() - mUnSelectedBitmap.getHeight(), mPaint);
                }
                if (isShowOccupiedImage && mOccupiedBitmapLeft != null && mOccupiedBitmapRight != null) {
                    canvas.drawBitmap(isLeft ? mOccupiedBitmapLeft : mOccupiedBitmapRight,
                            0, getHeight() - mOccupiedBitmapLeft.getHeight(), mPaint);
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec) + getPaddingLeft() + getPaddingRight(),
                MeasureSpec.getSize(heightMeasureSpec) + getPaddingTop() + getPaddingBottom());
    }

    interface OnItemPressedListener {
        void onPressed(int horizontalPos, int verticalPos);
    }
}
