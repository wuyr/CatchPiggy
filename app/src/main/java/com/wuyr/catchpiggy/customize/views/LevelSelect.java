package com.wuyr.catchpiggy.customize.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;

import com.wuyr.catchpiggy.R;
import com.wuyr.catchpiggy.customize.MyDrawable;
import com.wuyr.catchpiggy.utils.BitmapUtil;

/**
 * Created by wuyr on 17-12-31 下午8:13.
 */

public class LevelSelect extends ViewGroup {

    private AnimationButton mItems[];
    private MyDrawable mItemDrawable, mItemDrawableDisable;
    private int mItemSize;
    private int mMaxCount;
    private OnLevelSelectedListener mOnLevelSelectedListener;

    public LevelSelect(Context context) {
        this(context, null);
    }

    public LevelSelect(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LevelSelect(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mItemSize = (int) getResources().getDimension(R.dimen.xhpx_128);
        mItemDrawable = new MyDrawable(0, BitmapUtil.getBitmapFromResource(getContext(), R.mipmap.ic_level_select_bg));
        mItemDrawableDisable = new MyDrawable(0, BitmapUtil.toGray(mItemDrawable.getBitmap()));
    }

    public void setValidItemCount(int count) {
        if (count > mMaxCount) {
            count = mMaxCount;
        }
        if (mItems != null) {
            for (int i = 0; i < count; i++) {
                AnimationButton item = mItems[i];
                item.setText(String.valueOf(i + 1));
                item.setBackground(mItemDrawable);
                item.setEnabled(true);
            }
        }
    }

    public void setMaxItemCount(int count) {
        //already initialed
        if (getChildCount() > 0) {
            removeAllViews();
        }
        mMaxCount = count;
        mItems = new AnimationButton[count];
        float textSize = getResources().getDimension(R.dimen.xhpx_48);
        OnClickListener onClickListener = v -> {
            if (mOnLevelSelectedListener != null) {
                mOnLevelSelectedListener.onSelected((int) v.getTag());
            }
        };
        for (int i = 0; i < count; i++) {
            AnimationButton temp = new AnimationButton(getContext());
            temp.setBackground(mItemDrawableDisable);
            temp.setTag(i + 1);
            temp.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            temp.setTextColor(Color.WHITE);
            temp.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD_ITALIC));
            temp.setGravity(Gravity.CENTER);
            temp.setOnClickListener(onClickListener);
            temp.setEnabled(false);
            mItems[i] = temp;
            addView(temp);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = mItemSize * 5, height = 0;
        if (mItems != null) {
            int heightCount = mItems.length / 5;
            if (mItems.length % 5 > 0) {
                heightCount++;
            }
            height = mItemSize * heightCount;
        }
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (mItems != null) {
            int maxWidth = mItemSize * 4;
            int currentWidth;
            int currentHeight = -mItemSize;
            for (int i = 0; i < mItems.length; i++) {
                if (mItems[i] != null) {
                    currentWidth = i * mItemSize;
                    if (currentWidth >= maxWidth) {
                        currentWidth = i % 5 * mItemSize;
                    }
                    if (i % 5 == 0) {
                        currentHeight += mItemSize;
                    }
                    int left, top, right, bottom;
                    left = currentWidth;
                    right = currentWidth + mItemSize;
                    top = currentHeight;
                    bottom = currentHeight + mItemSize;
                    mItems[i].layout(left, top, right, bottom);
                }
            }
        }
    }

    public void release() {
        if (mItems != null) {
            for (AnimationButton tmp : mItems) {
                if (tmp != null) {
                    tmp.setBackground(null);
                    tmp.setOnClickListener(null);
                }
            }
            mItems = null;
        }
        mItemDrawable.release();
        mItemDrawableDisable.release();
        mOnLevelSelectedListener = null;
    }

    public void setOnLevelSelectedListener(OnLevelSelectedListener levelSelectedListener) {
        mOnLevelSelectedListener = levelSelectedListener;
    }

    public interface OnLevelSelectedListener {
        void onSelected(int level);
    }
}
