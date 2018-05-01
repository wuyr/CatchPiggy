package com.wuyr.catchpiggy.customize;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.SystemClock;

import com.wuyr.catchpiggy.models.PropData;
import com.wuyr.catchpiggy.utils.LogUtil;
import com.wuyr.catchpiggy.utils.ThreadPool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by wuyr on 17-12-8 上午2:50.
 */

/**
 * 修猪圈模式中的树头(未放置)位置更新辅助类
 */
public class PropOffsetHelper {

    private float mPropOffsetSpeed;//树头的移动速度
    private MyDrawable mPropDrawable;//树头的图片
    private List<PropData> mProps;//全部树头的数据
    private List<Integer> mLeavedProps;//已放置的树头(索引)
    private float mStartX, mStartY;//树头一开始的位置
    private int mPropSize;//树头尺寸
    private Future mUpdateTask;//更新位置的线程
    private float mLeftOffset;//左边的偏移量
    private volatile boolean isNeed;//是否需要更新位置
    private long mLastStopTime;//上一次暂停的时间

    public PropOffsetHelper(Bitmap propBitmap, int width, int height, int propSize) {
        mPropDrawable = new MyDrawable(0, propBitmap);
        mProps = new ArrayList<>();
        mLeftOffset = (propSize - mPropDrawable.getIntrinsicWidth()) / 2;
        mStartX = width + mLeftOffset;
        mStartY = height - mPropDrawable.getIntrinsicHeight();
        mPropSize = propSize;
        mLeavedProps = new ArrayList<>();
        long propOffsetAnimationDuration = 4000L;
        mPropOffsetSpeed = mStartX / propOffsetAnimationDuration;
    }

    public MyDrawable getPropDrawable(int index) {
        return mProps.get(index).drawable;
    }

    /**
     * 判断该索引的树头是否已经放置
     */
    public boolean isPropLeaved(int index) {
        return mLeavedProps.contains(index);
    }

    /**
     * 获取已放置的树头数量
     */
    public int getLeavedPropCount() {
        return mLeavedProps.size();
    }

    /**
     * 添加一个树头
     */
    public void addProp() {
        PropData data = new PropData(mPropDrawable.clone());
        data.setY(mStartY);
        data.setX(mStartX);
        mProps.add(data);
        LogUtil.print("添加了一个prop");
        //LogUtil.print(mProps.size());
    }

    public void setX(int index, float x) {
        mProps.get(index).setX(x);
    }

    public void setY(int index, float y) {
        mProps.get(index).setY(y);
    }

    /**
     * 有树头脱队
     */
    public void propLeaved(int index) {
        mLeavedProps.add(index);
        LogUtil.print("出队pos:" + index);
    }

    /**
     * 画队列中的(未放置的)树头
     */
    public void drawInQueueProps(Canvas canvas) {
        for (int i = 0; i < mProps.size(); i++) {
            if (!mLeavedProps.contains(i)) {
                mProps.get(i).draw(canvas);
            }
        }
    }

    /**
     * 画已放置的树头
     */
    public void drawLeavedProps(Canvas canvas) {
        for (int i = 0; i < mLeavedProps.size(); i++) {
            mProps.get(mLeavedProps.get(i)).draw(canvas);
        }
    }

    public int size() {
        return mProps.size();
    }

    //树头高度
    public int getPropHeight() {
        return mPropDrawable.getIntrinsicHeight();
    }

    //树头宽度
    public int getPropWidth() {
        return mPropDrawable.getIntrinsicWidth();
    }

    /**
     * 停止生成
     */
    public void stop() {
        isNeed = false;
        if (mUpdateTask != null) {
            mUpdateTask.cancel(true);
            mUpdateTask = null;
        }
        mLastStopTime = SystemClock.uptimeMillis();
    }

    //获取队列中的(未放置的)树头数量
    public int getQueueSize() {
        return mProps.size() - mLeavedProps.size();
    }

    /**
     * 开始更新树桩的位置
     */
    public void startComputeOffset() {
        updatePropGenerateTime();
        isNeed = true;
        //更新树头位置线程
        mUpdateTask = ThreadPool.getInstance().execute(() -> {
            boolean isFinished;//树头是否已经到对应的位置
            float distance,//需要偏移的路程
                    offset;//本次更新的偏移量
            int hitOffsetCount;//排在该树头前面的,并且已经离队的(已放置),需要忽略距离
            long intervalTime,//上次更新与现在的间隔时间
                    updateTime;//今次更新时间
            while (isNeed) {
                for (int i = 0; i < mProps.size(); i++) {
                    PropData prop = mProps.get(i);
                    //已离队的不需要更新位置
                    if (mLeavedProps.contains(i)) {
                        continue;
                    }
                    //计算出总距离
                    distance = i * mPropSize + mLeftOffset;
                    //离队树桩数量
                    hitOffsetCount = 0;
                    for (int j = 0; j < mLeavedProps.size(); j++) {
                        //检查是否有离队的树头
                        if (mLeavedProps.get(j) < i) {
                            hitOffsetCount++;
                        }
                    }
                    //减去已离队的树桩占用的位置，得出真实的位置
                    distance -= mPropSize * hitOffsetCount;
                    //树桩的x轴小于或等于实际的偏移距离，则认为已经偏移完成，不需要继续更新位置
                    isFinished = prop.getX() <= distance;
                    updateTime = SystemClock.uptimeMillis();
                    if (!isFinished) {
                        //计算间隔时间
                        intervalTime = updateTime - prop.lastUpdateTime;
                        //路程 = 时间 * 速度
                        offset = intervalTime * mPropOffsetSpeed;
                        //更新x轴位置
                        prop.setX(prop.getX() - offset);
                    }
                    //刷新上一次的更新时间
                    prop.lastUpdateTime = updateTime;
                }
            }
        });
    }

    /**
     * 更新线程停止后又重新开始,需要加上停止的这段时间
     */
    private void updatePropGenerateTime() {
        if (mLastStopTime > 0) {
            //总停止时间 = 当前时间 - 上次更新时间
            long totalStoppedTime = SystemClock.uptimeMillis() - mLastStopTime;
            mLastStopTime = 0;
            for (int i = 0; i < mProps.size(); i++) {
                //加上这段时间
                mProps.get(i).lastUpdateTime += totalStoppedTime;
            }
        }
    }

    public void release() {
        stop();
        if (mPropDrawable != null) {
            mPropDrawable.release();
            mPropDrawable = null;
        }
        if (mProps != null) {
            for (PropData propData : mProps) {
                if (propData != null) {
                    propData.release();
                }
            }
        }
        mProps = null;
        mLeavedProps = null;
    }
}
