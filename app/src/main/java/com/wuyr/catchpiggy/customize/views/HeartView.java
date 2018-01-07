package com.wuyr.catchpiggy.customize.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.wuyr.catchpiggy.R;
import com.wuyr.catchpiggy.customize.MyDrawable;
import com.wuyr.catchpiggy.utils.BitmapUtil;

/**
 * Created by wuyr on 18-1-4 下午10:05.
 */

public class HeartView extends ViewGroup {

    private MyDrawable mHeartFill, mHeartStroke;
    private ImageView[] mItems;
    private int mItemSize, mItemMargin;
    private int mCurrentValidHeartCount;

    public HeartView(Context context) {
        this(context, null);
    }

    public HeartView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HeartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mItems = new ImageView[5];
        mItemSize = (int) getResources().getDimension(R.dimen.xhpx_76);
        mHeartFill = new MyDrawable(0, BitmapUtil.getBitmapFromResource(getContext(), R.mipmap.ic_heart_fill));
        mHeartStroke = new MyDrawable(0, BitmapUtil.getBitmapFromResource(getContext(), R.mipmap.ic_heart_stroke));
        mItemMargin = (int) getResources().getDimension(R.dimen.xhpx_6);
        for (int i = 0; i < mItems.length; i++) {
            ImageView temp = new ImageView(getContext());
            mItems[i] = temp;
            addView(temp);
        }
    }

    public void setValidHeartCount(int count) {
        if (count > mItems.length) {
            count = mItems.length;
        } else if (count < 0) {
            count = 0;
        }
        mCurrentValidHeartCount = count;
        for (int i = 0; i < mItems.length; i++) {
            mItems[i].setImageDrawable(i < count ? mHeartFill : mHeartStroke);
        }
    }

    public int getCurrentValidHeartCount() {
        return mCurrentValidHeartCount;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(mItemSize * mItems.length + (mItems.length - 1) * mItemMargin, mItemSize);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int currentWidth;
        int currentMargin;
        for (int i = 0; i < mItems.length; i++) {
            currentWidth = i * mItemSize;
            currentMargin = i * mItemMargin;
            currentWidth += currentMargin;
            mItems[i].layout(currentWidth, 0, currentWidth + mItemSize, mItemSize);
        }
    }

    public void release() {
        if (mItems != null) {
            for (ImageView tmp : mItems) {
                if (tmp != null) {
                    tmp.setImageDrawable(null);
                }
            }
            mItems = null;
        }
        mHeartFill.release();
        mHeartStroke.release();
        mHeartFill = null;
        mHeartStroke = null;
    }
}
