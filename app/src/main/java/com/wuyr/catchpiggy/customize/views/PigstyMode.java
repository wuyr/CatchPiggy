package com.wuyr.catchpiggy.customize.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.NinePatch;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import com.wuyr.catchpiggy.Application;
import com.wuyr.catchpiggy.R;
import com.wuyr.catchpiggy.activities.MainActivity;
import com.wuyr.catchpiggy.customize.MyDrawable;
import com.wuyr.catchpiggy.customize.MyPath;
import com.wuyr.catchpiggy.customize.MyValueAnimator;
import com.wuyr.catchpiggy.customize.Pig;
import com.wuyr.catchpiggy.customize.PropOffsetHelper;
import com.wuyr.catchpiggy.models.MissionData;
import com.wuyr.catchpiggy.models.WayData;
import com.wuyr.catchpiggy.utils.BitmapUtil;
import com.wuyr.catchpiggy.utils.ComputeWayUtil;
import com.wuyr.catchpiggy.utils.LevelUtil;
import com.wuyr.catchpiggy.utils.ShareUtil;
import com.wuyr.catchpiggy.utils.ThreadPool;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * Created by wuyr on 17-11-12 下午11:20.
 */

public class PigstyMode extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private static final int MAX_PROP_SIZE;
    private static final int PIGGY_COUNT;
    private static final int VERTICAL_COUNT;
    private static final int HORIZONTAL_COUNT;
    private static final long FENCE_FIX_ANIMATION_DURATION;
    private final byte[] PROP_GENERATE_TASK_LOCK;
    private String mLevelStringFormat;
    private String mCarIsComingText, mPiggiesHasRunText, mStartCatchText, mDragPigText;
    private Future mDrawTask, mPropGenerateTask;
    private SurfaceHolder mSurfaceHolder;
    private Rect[][] mItems;
    private volatile int[][] mItemStatus;
    private Pig[] mPiggies;
    private volatile boolean isDrawing;
    private int mPropSize, mItemSize;
    private Set<Integer> mDraggingProps, mDraggingPiggies;
    private SparseIntArray mDraggingPropIds, mDraggingPiggyIds;
    private Future[] mComputePathTasks;
    private PropOffsetHelper mPropOffsetHelper;
    private WayData[] mPiggiesOccupiedPosition;
    private Bitmap mFrameBackgroundBitmap;
    private NinePatch mFrameBackground;
    private MyDrawable mCarHead, mCarBody;
    private volatile boolean isNeed;
    private int mWidth, mHeight;
    private int mLeftOffset;
    private int mTop;
    private volatile boolean isStopped;
    private volatile boolean isPiggyByCar;
    private boolean isFirstInit, isGameOver, isWon, isCarDispatched, isCarArrived, isAllPiggiesAreReady;
    private boolean[] mCarriageIsOccupied;
    private TextPaint mPaint;
    private List<Integer> mCaughtPiggies;
    private SparseIntArray mCaughtPiggiesPosition;
    private long mStartTime;
    private int mCurrentLevel;
    private boolean isMissionDialogShown;
    private int mValidCaughtCount;
    private MissionData mMissionData;

    static {
        MAX_PROP_SIZE = 6;
        HORIZONTAL_COUNT = 15;
        VERTICAL_COUNT = 23;
        PIGGY_COUNT = 6;
        FENCE_FIX_ANIMATION_DURATION = 150L;
    }

    {
        PROP_GENERATE_TASK_LOCK = new byte[0];
    }

    public PigstyMode(Context context) {
        this(context, null);
    }

    public PigstyMode(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        isFirstInit = true;
        setZOrderOnTop(true);
        mSurfaceHolder = getHolder();
        mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
        mSurfaceHolder.addCallback(this);
        mCaughtPiggies = new ArrayList<>();
        mDraggingProps = new HashSet<>();
        mDraggingPiggies = new HashSet<>();
        mDraggingPropIds = new SparseIntArray();
        mDraggingPiggyIds = new SparseIntArray();
        mCaughtPiggiesPosition = new SparseIntArray();
        mComputePathTasks = new Future[PIGGY_COUNT];
        mPiggiesOccupiedPosition = new WayData[PIGGY_COUNT];
        mCarriageIsOccupied = new boolean[PIGGY_COUNT];
        mItemSize = (int) getContext().getResources().getDimension(R.dimen.xhpx_64);
        mPropSize = (int) getContext().getResources().getDimension(R.dimen.xhpx_108);
        mFrameBackgroundBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_frame);
        mFrameBackground = new NinePatch(mFrameBackgroundBitmap, mFrameBackgroundBitmap.getNinePatchChunk(), null);
        mLevelStringFormat = getContext().getString(R.string.level_format);
        initCar();
        initPaint();
        initPiggies();
    }

    private void initPaint() {
        mPaint = new TextPaint();
        mPaint.setAntiAlias(true);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTextSize(getResources().getDimension(R.dimen.xhpx_38));
        mPaint.setColor(getResources().getColor(R.color.colorHint));

        mCarIsComingText = getResources().getString(R.string.car_is_coming);
        mPiggiesHasRunText = getResources().getString(R.string.piggies_has_run);
        mStartCatchText = getResources().getString(R.string.start_catch);
        mDragPigText = getResources().getString(R.string.drag_pig);
    }

    private void initPiggies() {
        mPiggies = new Pig[PIGGY_COUNT];
        Pig.OnTouchListener onTouchListener = (pig, event, index) -> {
            switch (event.getAction() & event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
                    pigActionDown(pig, event, index);
                    break;
                case MotionEvent.ACTION_MOVE:
                    pigActionMove(pig, event, index);
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                    pigActionUp(pig, false);
                    break;
                default:
                    break;
            }
        };
        Pig.OnPositionUpdateListener onPositionUpdateListener = (pig, oldPosition, newPosition) -> {
            changeItemStatus(oldPosition.y, oldPosition.x, Item.STATE_UNSELECTED);
            if (pig.getState() == Pig.STATE_RUNNING) {
                changeItemStatus(newPosition.y, newPosition.x, Item.STATE_OCCUPIED);
                pig.setPosition(newPosition.y, newPosition.x);
                mPiggiesOccupiedPosition[pig.getIndex()] = newPosition;
            }
        };
        Pig.OnLeavedListener onLeavedListener = this::checkIsGameOver;
        float scale = mItemSize / (mItemSize * 1.15F);
        for (int i = 0; i < PIGGY_COUNT; i++) {
            Pig pig = new Pig(getContext(), scale);
            pig.setIndex(i);
            pig.setOnTouchListener(onTouchListener);
            pig.setOnPositionUpdateListener(onPositionUpdateListener, mItemSize);
            pig.setOnLeavedListener(onLeavedListener);
            mPiggies[i] = pig;
        }
    }

    private void changeItemStatus(int vertical, int horizontal, int newState) {
        mItemStatus[vertical][horizontal] = newState;
    }

    private void stopTask(Pig pig) {
        if (mComputePathTasks[pig.getIndex()] != null) {
            mComputePathTasks[pig.getIndex()].cancel(true);
            mComputePathTasks[pig.getIndex()] = null;
        }
        pig.cancelPathAnimation();
        pig.stopTranslateAnimation();
    }

    private void initRunAnimation(Pig pig, final boolean isPlayAnimation) {
        WayData pos = pig.getPosition();
        Rect rect = mItems[pos.y][pos.x];
        int offsetX = (mItemSize - pig.getWidth()) / 2 + (pig.getOrientation() == Pig.ORIENTATION_LEFT ? -mItemSize / 2 : (int) -(pig.getWidth() * .33)) + mItemSize / 2;
        int offsetY = (int) ((mItemSize / 2) - (pig.getHeight() * .75));
        mComputePathTasks[pig.getIndex()] = ThreadPool.getInstance().execute(() -> {
            if (!isPlayAnimation) {
                MyValueAnimator.create(pig.getX(), rect.left + offsetX, pig.getY(), rect.top + offsetY, pig).setDuration(FENCE_FIX_ANIMATION_DURATION).setRunOnCurrentThread().start();
            }
            pig.setState(Pig.STATE_STANDING);
            if (Thread.interrupted()) {
                return;
            }
            List<WayData> wayDataList = ComputeWayUtil.findWay4(mItemStatus, mPiggiesOccupiedPosition, pig.getPosition());
            if (Thread.interrupted()) {
                return;
            }
            if (wayDataList != null && wayDataList.size() > 1) {
                MyPath path = initPath(pig, wayDataList, offsetX, offsetY, isPlayAnimation);
                pig.setPathAnimation(path, wayDataList).setDuration(wayDataList.size() * mMissionData.speed)
                        .setRepeat(!ComputeWayUtil.isEdge(VERTICAL_COUNT, HORIZONTAL_COUNT, wayDataList.get(wayDataList.size() - 1)));
                if (Thread.interrupted()) {
                    return;
                }
                pig.startPathAnimation();
            } else {
                if (Thread.interrupted()) {
                    return;
                }
                pig.startTranslateAnimation(rect.left + offsetX, rect.top + offsetY);
            }
            pig.setEnable(true);
            checkIsGameOver();
        });
    }

    private void checkIsGameOver() {
        if (!isGameOver) {
            int caughtCount = 0;
            int runCount = 0;
            mCaughtPiggies.clear();
            for (int i = 0; i < PIGGY_COUNT; i++) {
                Pig pig = mPiggies[i];
                if (pig.isRepeatAnimation()) {
                    caughtCount++;
                    mCaughtPiggies.add(pig.getIndex());
                } else if (pig.getState() == Pig.STATE_STANDING) {
                    if (ComputeWayUtil.isEdge(VERTICAL_COUNT, HORIZONTAL_COUNT, pig.getPosition())) {
                        runCount++;
                    } else {
                        int[][] pattern = new int[VERTICAL_COUNT][HORIZONTAL_COUNT];
                        for (int vertical = 0; vertical < VERTICAL_COUNT; vertical++) {
                            System.arraycopy(mItemStatus[vertical], 0, pattern[vertical], 0, HORIZONTAL_COUNT);
                        }
                        if (ComputeWayUtil.getCanArrivePos(pattern, pig.getPosition()).isEmpty()) {
                            caughtCount++;
                            if (!mCaughtPiggies.contains(i)) {
                                mCaughtPiggies.add(pig.getIndex());
                            }
                        }
                    }
                }
            }
            if (caughtCount + runCount >= PIGGY_COUNT) {
                isWon = caughtCount > 0;
                initGameOver();
            }
        }
    }

    private void initGameOver() {
        isGameOver = true;
        if (isWon) {
            //车来到之前不能操作，避免猪走了
            for (Pig pig: mPiggies) {
               pig.setEnable(false);
            }
            mCarHead.setY(0);
            MyValueAnimator.create(mCarHead.getX(), 0, 0, 0, mCarHead)
                    .setDuration(5000).setOnAnimatorEndListener(() -> {
                isCarDispatched = true;
                isCarArrived = true;
                //车到了之后才恢复
                for (Pig pig: mPiggies) {
                    pig.setEnable(true);
                }
                ThreadPool.getInstance().execute(() -> {
                    //8秒还没有将全部已捉到的小猪放到车上，就把车开走
                    try {
                        Thread.sleep(8000L);
                    } catch (InterruptedException e) {
                        return;
                    }
                    //不让操作了
                    for (Pig pig: mPiggies) {
                        pig.setEnable(false);
                    }
                    //将车开走
                    MyValueAnimator animator = MyValueAnimator.create(mCarHead.getX(), -(mCarHead.getIntrinsicWidth() * 2 + mCarBody.getIntrinsicWidth() * 6), mCarHead.getY(), mCarHead.getY(), mCarHead)
                            .setDuration(4000).setOnAnimatorEndListener(this::gameOver);
                    if (!isAllPiggiesAreReady) {
                        isAllPiggiesAreReady = true;
                        animator.start();
                    }
                });
            }).start();
        } else {
            gameOver();
        }
    }

    private MyPath initPath(Pig pig, List<WayData> data, int offsetX, int offsetY, boolean isPlayAnimation) {
        MyPath path = new MyPath();
        Rect item = mItems[data.get(0).y][data.get(0).x];
        if (isPlayAnimation) {
            path.moveTo(pig.getX(), pig.getY());
            path.getData().remove(0);
            path.lineTo(item.left + offsetX, item.top + offsetY);
        } else {
            path.moveTo(item.left + offsetX, item.top + offsetY);
        }
        WayData removedItem = data.remove(0);
        Queue<WayData> queue = new ArrayDeque<>(data);
        while (!queue.isEmpty()) {
            item = mItems[queue.peek().y][queue.poll().x];
            if (!queue.isEmpty()) {
                Rect item2 = mItems[queue.peek().y][queue.poll().x];
                path.quadTo(item.left + offsetX, item.top + offsetY, item2.left + offsetX, item2.top + offsetY);
            } else {
                path.lineTo(item.left + offsetX, item.top + offsetY);
            }
        }
        data.add(0, removedItem);
        return path;
    }

    private void setProp(int index) {
        if (mPropOffsetHelper.isPropLeaved(index)) {
            MyDrawable fence = mPropOffsetHelper.getPropDrawable(index);
            for (int vertical = 0; vertical < VERTICAL_COUNT; vertical++) {
                for (int horizontal = 0; horizontal < HORIZONTAL_COUNT; horizontal++) {
                    Rect item = mItems[vertical][horizontal];
                    int x = (int) (fence.getX() + fence.getIntrinsicWidth() / 2);
                    int y = (int) (fence.getY() + fence.getIntrinsicHeight() * .66);
                    if (item.contains(x, y)) {
                        List<WayData> data = new ArrayList<>();
                        if (!ComputeWayUtil.isCurrentPositionCanSet(mItemStatus, mPiggiesOccupiedPosition, new WayData(horizontal, vertical), data)) {
                            if (data.isEmpty()) {
                                for (Pig pig : mPiggies) {
                                    if (pig.getState() == Pig.STATE_RUNNING) {
                                        data.addAll(pig.getPathData());
                                    }
                                }
                            }
                            WayData position = ComputeWayUtil.findNextUnSelected(mItemStatus, data, new WayData(horizontal, vertical));
                            if (position != null) {
                                vertical = position.y;
                                horizontal = position.x;
                            }
                        } else if (mItemStatus[vertical][horizontal] != Item.STATE_UNSELECTED) {
                            WayData position = ComputeWayUtil.findNextUnSelected(mItemStatus, null, new WayData(horizontal, vertical));
                            if (position != null) {
                                vertical = position.y;
                                horizontal = position.x;
                            }
                        }
                        changeItemStatus(vertical, horizontal, Item.STATE_SELECTED);

                        Rect rect = mItems[vertical][horizontal];
                        MyValueAnimator.create(fence.getX(), rect.left, fence.getY(), rect.top + (mItemSize - fence.getIntrinsicHeight()), fence).setDuration(FENCE_FIX_ANIMATION_DURATION).start();
                        positionOccupied(vertical, horizontal);
                        return;
                    }
                }
            }
        }
    }

    private void positionOccupied(int vertical, int horizontal) {
        for (int i = 0; i < PIGGY_COUNT; i++) {
            Pig pig = mPiggies[i];
            List<WayData> pathData = pig.getPathData();
            if (pathData == null || pig.getState() != Pig.STATE_RUNNING) {
                continue;
            }
            int currentIndex = -1;
            if (pig.isRepeatAnimation()) {
                currentIndex = 0;
            } else {
                for (int j = 0; j < pathData.size(); j++) {
                    WayData pos = pig.getPosition();
                    if (pathData.get(j).equals(pos)) {
                        currentIndex = j;
                        break;
                    }
                }
            }
            if (currentIndex != -1) {
                for (int k = currentIndex; k < pathData.size(); k++) {
                    if (pathData.get(k).x == horizontal && pathData.get(k).y == vertical) {
                        stopTask(pig);
                        pig.setState(Pig.STATE_STANDING);
                        initRunAnimation(pig, true);
                        break;
                    }
                }
            }
        }
    }

    private void initItems() {
        mItems = new Rect[VERTICAL_COUNT][HORIZONTAL_COUNT];
        mItemStatus = new int[VERTICAL_COUNT][HORIZONTAL_COUNT];
        int currentX, currentY;
        int childrenY = (getHeight() - mItemSize * VERTICAL_COUNT - mItemSize) / 2 + mItemSize / 2,
                childrenX = (getWidth() - mItemSize * HORIZONTAL_COUNT - mItemSize) / 2 + mItemSize / 2;
        for (int vertical = 0; vertical < VERTICAL_COUNT; vertical++) {
            currentY = mItemSize * vertical;
            for (int horizontal = 0; horizontal < HORIZONTAL_COUNT; horizontal++) {
                currentX = mItemSize * horizontal + (vertical % 2 == 0 ? mItemSize / 2 : 0);
                Rect rect = new Rect(childrenX + currentX, childrenY + currentY,
                        childrenX + currentX + mItemSize, childrenY + currentY + mItemSize);
                mItems[vertical][horizontal] = rect;
                changeItemStatus(vertical, horizontal, Item.STATE_UNSELECTED);
            }
        }
    }

    private void startGenerate() {
        isNeed = true;
        startGenerateProp();
        mPropOffsetHelper.restart();
    }

    private void stopGenerate() {
        isNeed = false;
        if (mDrawTask != null) {
            mDrawTask.cancel(true);
            mDrawTask = null;
        }
        if (mPropGenerateTask != null) {
            mPropGenerateTask.cancel(true);
            mPropGenerateTask = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (isMissionDialogShown) {
            restart();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stop();
    }

    public boolean isStopped() {
        return isStopped;
    }

    public void restart() {
        isStopped = false;
        if (mStartTime == 0) {
            mStartTime = SystemClock.uptimeMillis();
        }
        if (mItems == null) {
            initItems();
        }
        if (mPropOffsetHelper == null) {
            initPropOffsetHelper();
        }
        mWidth = getWidth();
        mHeight = getHeight();
        mTop = getHeight() - mPropOffsetHelper.getPropHeight();
        mLeftOffset = (mPropSize - mPropOffsetHelper.getPropWidth()) / 2;
        isDrawing = true;
        mDrawTask = ThreadPool.getInstance().execute(this);
        if (isFirstInit) {
            playInitAnimation();
        } else {
            if (!isGameOver) {
                startGenerate();
            }
        }
    }

    private void initPropOffsetHelper() {
        Bitmap propBitmap = BitmapUtil.getBitmapFromResource(getContext(), R.mipmap.ic_prop);
        float scale = (float) mItemSize / propBitmap.getWidth();
        propBitmap = BitmapUtil.scaleBitmap(propBitmap, (int) (propBitmap.getWidth() * scale), (int) (propBitmap.getHeight() * scale));
        mPropOffsetHelper = new PropOffsetHelper(propBitmap, getWidth(), getHeight(), mPropSize);
    }

    public void stop() {
        isStopped = true;
        isDrawing = false;
        stopGenerate();
        if (mPropOffsetHelper != null) {
            mPropOffsetHelper.stop();
        }
    }

    @Override
    public void run() {
        while (isDrawing) {
            Canvas canvas = mSurfaceHolder.lockCanvas();
            if (canvas == null) {
                return;
            }
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            startDraw(canvas);
            mSurfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    private void startDraw(Canvas canvas) {
        mPropOffsetHelper.drawLeavedProps(canvas);
        //图层顺序不同
        if (!isCarArrived) {
            boolean isNeedDrawPiggies = isPiggyByCar;
            if (!isNeedDrawPiggies) {
                for (Pig pig : mPiggies) {
                    if (!pig.isInitialized) {
                        pig.isInitialized = true;
                        pigActionUp(pig, true);
                    }
                    pig.getCurrentDrawable().draw(canvas);
                }
            }
            //bottom frame bg
            mFrameBackground.draw(canvas, new RectF(0, mTop, mWidth, mHeight));
            mPropOffsetHelper.drawInQueueProps(canvas);
            //top frame bg
            mFrameBackground.draw(canvas, new RectF(0, 0, mWidth, mCarHead.getIntrinsicHeight()));
            drawCar(canvas, isNeedDrawPiggies);
        } else {
            //top frame bg
            mFrameBackground.draw(canvas, new RectF(0, 0, mWidth, mCarHead.getIntrinsicHeight()));
            //draw car
            mCarHead.draw(canvas);
            //为了避免重复判断（有可能两次结果不一样，导致本次draw没有draw）
            boolean[] isCarriageDrawn = new boolean[PIGGY_COUNT];
            for (int i = 0; i < PIGGY_COUNT; i++) {
                if (!mCarriageIsOccupied[i]) {
                    int offset = i * mCarBody.getIntrinsicWidth();
                    mCarBody.setX(mCarHead.getX() + mCarHead.getIntrinsicWidth() + offset);
                    mCarBody.setY(mCarHead.getY());
                    mCarBody.draw(canvas);
                    isCarriageDrawn[i] = true;
                }
            }
            //draw piggies
            for (Pig pig : mPiggies) {
                if (!pig.isInitialized) {
                    pig.isInitialized = true;
                    pigActionUp(pig, true);
                }
                //不画在车上的，在下面画
                if (isAllPiggiesAreReady) {
                    if (!mCaughtPiggies.contains(pig.getIndex())) {
                        pig.getCurrentDrawable().draw(canvas);
                    }
                } else {
                    pig.getCurrentDrawable().draw(canvas);
                }
            }
            //画上没有画的
            for (int i = 0; i < PIGGY_COUNT; i++) {
                int offset = i * mCarBody.getIntrinsicWidth();
                if (isAllPiggiesAreReady && mCarriageIsOccupied[i]) {
                    Pig pig = mPiggies[mCaughtPiggiesPosition.get(i)];
                    pig.setY(mCarHead.getY() + mCarHead.getIntrinsicHeight() - pig.getHeight());
                    int offset2 = (pig.getWidth() - mCarBody.getIntrinsicWidth()) / 2;
                    pig.setX(mCarHead.getX() + mCarHead.getIntrinsicWidth() + offset - offset2);
                    pig.getCurrentDrawable().draw(canvas);

                }
                if (!isCarriageDrawn[i]) {
                    mCarBody.setX(mCarHead.getX() + mCarHead.getIntrinsicWidth() + offset);
                    mCarBody.setY(mCarHead.getY());
                    mCarBody.draw(canvas);
                }
            }
            //bottom frame bg
            mFrameBackground.draw(canvas, new RectF(0, mTop, mWidth, mHeight));
            mPropOffsetHelper.drawInQueueProps(canvas);
        }
        drawText(canvas);
    }

    private void drawCar(Canvas canvas, boolean isNeedDrawPiggies) {
        mCarHead.draw(canvas);
        for (int i = 0; i < PIGGY_COUNT; i++) {
            int offset = i * mCarBody.getIntrinsicWidth();
            Pig pig = mPiggies[i];
            if (isNeedDrawPiggies) {
                pig.setY(mCarHead.getY() + mCarHead.getIntrinsicHeight() - pig.getHeight());
                int offset2 = (pig.getWidth() - mCarBody.getIntrinsicWidth()) / 2;
                pig.setX(mCarHead.getX() + mCarHead.getIntrinsicWidth() + offset - offset2);
                pig.getCurrentDrawable().draw(canvas);
            }
            mCarBody.setX(mCarHead.getX() + mCarHead.getIntrinsicWidth() + offset);
            mCarBody.setY(mCarHead.getY());
            mCarBody.draw(canvas);
        }
    }

    private void drawText(Canvas canvas) {
        String text = null;
        if (!isCarDispatched) {
            if (isGameOver) {
                text = isWon ? mCarIsComingText : mPiggiesHasRunText;
            } else if (!isPiggyByCar) {
                text = mStartCatchText;
            } else {
                if (mCurrentLevel > 0) {
                    text = String.format(mLevelStringFormat, mCurrentLevel);
                }
            }
            if (text != null) {
                StaticLayout staticLayout = new StaticLayout(text, mPaint, getWidth(), Layout.Alignment.ALIGN_NORMAL, 1, 0, true);
                canvas.save();
                canvas.translate(mWidth / 2, 0);
                staticLayout.draw(canvas);
                canvas.restore();
            }
        } else {
            if (isWon && !isAllPiggiesAreReady) {
                float y = (mHeight - mPaint.getFontMetricsInt().bottom - mPaint.getFontMetricsInt().top) / 2;
                canvas.drawText(mDragPigText, mWidth / 2, y, mPaint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //还未初始化完成
        if (mPropOffsetHelper == null || mDraggingProps == null || mDraggingPiggies == null
                || mDraggingPropIds == null || mDraggingPiggyIds == null || mPiggies == null) {
            return false;
        }

        Point[] points = new Point[event.getPointerCount()];
        for (int i = 0; i < points.length; i++) {
            points[i] = new Point((int) event.getX(i), (int) event.getY(i));
        }
        switch (event.getAction() & event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                //只处理down事件的对应对象
                int currentIndex = event.getActionIndex();
                Point point = points[currentIndex];
                boolean isHandled = false;
                if (mDraggingPiggyIds.get(event.getPointerId(currentIndex), -1) == -1) {
                    for (int i = PIGGY_COUNT - 1; i >= 0; i--) {
                        Pig pig = mPiggies[i];
                        if (contains(pig.getX(), pig.getY(), pig.getX() + pig.getWidth(),
                                pig.getY() + pig.getHeight(), point.x, point.y)) {
                            mDraggingPiggies.add(i);
                            mDraggingPiggyIds.put(event.getPointerId(currentIndex), i);
                            mPiggies[i].onTouch(event, currentIndex);
                            isHandled = true;
                            break;
                        }
                    }
                }
                if (!isHandled && !isGameOver) {
                    for (int i = 0; i < mPropOffsetHelper.size(); i++) {
                        if (mPropOffsetHelper.isPropLeaved(i)) {
                            continue;
                        }
                        if (contains(mPropOffsetHelper.getPropDrawable(i), point.x, point.y)) {
//                            添加index
                            mDraggingProps.add(i);
//                            用id作为key,确保唯一性
                            mDraggingPropIds.put(event.getPointerId(currentIndex), i);
                            updatePosition(points[currentIndex], i);
                            break;
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                for (int pointIndex = 0; pointIndex < points.length; pointIndex++) {
                    for (int index : mDraggingProps) {
//                        根据id获取正确的index
                        if (mDraggingPropIds.get(event.getPointerId(pointIndex), -1) == index) {
                            updatePosition(points[pointIndex], index);
                        }
                    }
                    for (int index : mDraggingPiggies) {
                        if (mDraggingPiggyIds.get(event.getPointerId(pointIndex), -1) == index) {
                            mPiggies[index].onTouch(event, pointIndex);
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_OUTSIDE:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
//                根据当前up的index获取对应id
                int id = event.getPointerId(event.getActionIndex());
                int propIndex = mDraggingPropIds.get(id, -1);
                int piggyIndex = mDraggingPiggyIds.get(id, -1);
                if (propIndex != -1) {
                    if (!mPropOffsetHelper.isPropLeaved(propIndex)) {
                        mPropOffsetHelper.setY(propIndex, mTop);
                    }
                    mDraggingProps.remove(propIndex);
                    mDraggingPropIds.delete(id);
                    setProp(propIndex);
                }
                if (piggyIndex != -1) {
                    mPiggies[piggyIndex].onTouch(event, piggyIndex);
                    mDraggingPiggies.remove(piggyIndex);
                    mDraggingPiggyIds.delete(id);
                }
                break;
            default:
                break;
        }
        return true;
    }

    private void updatePosition(Point point, int index) {
        float offsetY = mPropSize / 2;
        float offsetX = mPropOffsetHelper.getPropWidth() / 2;
        float y = point.y - offsetY, x = point.x - offsetX;
        if (y < 0) {
            y = 0;
        }
        if (y + mPropOffsetHelper.getPropHeight() > getHeight()) {
            y = getHeight() - mPropOffsetHelper.getPropHeight();
        }

        if (y + mPropOffsetHelper.getPropHeight() < mTop) {
            if (!mPropOffsetHelper.isPropLeaved(index)) {
                mPropOffsetHelper.propLeaved(index);
                synchronized (PROP_GENERATE_TASK_LOCK) {
                    PROP_GENERATE_TASK_LOCK.notifyAll();
                }
            }
        }
        if (mPropOffsetHelper.isPropLeaved(index)) {
            if (x < 0) {
                x = 0;
            }
            if (x + mPropOffsetHelper.getPropWidth() > mWidth) {
                x = mWidth - mPropOffsetHelper.getPropWidth();
            }
            mPropOffsetHelper.setX(index, x);
            if (y + mPropOffsetHelper.getPropHeight() > mTop) {
                y = mTop - mPropOffsetHelper.getPropHeight();
            }
            mPropOffsetHelper.setY(index, y);
        } else {
            mPropOffsetHelper.setY(index, y);
        }
    }

    private boolean contains(MyDrawable drawable, float x, float y) {
        return contains(drawable.getX() - mLeftOffset, drawable.getY(),
                drawable.getX() + drawable.getIntrinsicWidth() + mLeftOffset,
                drawable.getY() + drawable.getIntrinsicHeight(), x, y);
    }

    private boolean contains(float left, float top, float right, float bottom, float x, float y) {
        return x >= left && x < right && y >= top && y < bottom;
    }

    private void startGenerateProp() {
        mPropGenerateTask = ThreadPool.getInstance().execute(() -> {
            while (isNeed) {
                synchronized (PROP_GENERATE_TASK_LOCK) {
                    while (mPropOffsetHelper.getQueueSize() >= MAX_PROP_SIZE) {
                        try {
                            PROP_GENERATE_TASK_LOCK.wait();
                        } catch (InterruptedException e) {
                            return;
                        }
                    }
                }
                try {
                    Thread.sleep(mMissionData.propDelay);
                } catch (InterruptedException e) {
                    return;
                }
                if (!isNeed) {
                    return;
                }
                mPropOffsetHelper.addProp();
            }
        });
    }

    private void playInitAnimation() {
        isPiggyByCar = true;
        mCarHead.setX(mWidth + mCarHead.getIntrinsicWidth());
        float y = mHeight / 2 - mCarHead.getIntrinsicHeight();
        mCarHead.setY(y);
        MyValueAnimator.create(mWidth, -(mCarHead.getIntrinsicWidth() * 2 + mCarBody.getIntrinsicWidth() * 6), y, y, mCarHead)
                .setDuration(10000).setOnAnimatorMiddleListener(() -> {
            isPiggyByCar = false;
            startGenerate();
        }).start();
        isFirstInit = false;
    }

    private void pigActionMove(Pig pig, MotionEvent event, int index) {
        if (pig.getState() != Pig.STATE_DRAGGING) {
            pig.setState(Pig.STATE_DRAGGING);
        }
        pig.setX(event.getX(index) - pig.getWidth() / 2);
        pig.setY(event.getY(index) - pig.getWidth() / 2);
    }

    private void pigActionDown(Pig pig, MotionEvent event, int index) {
        stopTask(pig);
        pig.setState(Pig.STATE_DRAGGING);
        WayData pos = pig.getPosition();
        changeItemStatus(pos.y, pos.x, Item.STATE_UNSELECTED);
        mPiggiesOccupiedPosition[pig.getIndex()] = null;
        pigActionMove(pig, event, index);
    }

    private void pigActionUp(Pig pig, boolean isPlayAnimation) {
        for (int vertical = 0; vertical < VERTICAL_COUNT; vertical++) {
            for (int horizontal = 0; horizontal < HORIZONTAL_COUNT; horizontal++) {
                Rect item = mItems[vertical][horizontal];
                int x = (int) (pig.getX() + pig.getDragWidth() * (pig.getOrientation() == Pig.ORIENTATION_LEFT ? .75 : .25));
                int y = (int) (pig.getY() + pig.getHeight() * .66);
                if (isCarArrived) {
                    if (x < 0 || x > mWidth || y > mCarHead.getIntrinsicHeight()) {
                        WayData pos = pig.getPosition();
                        float oldX = mItems[pos.y][pos.x].left, oldY = mItems[pos.y][pos.x].top;
                        MyValueAnimator.create(pig.getX(), oldX, pig.getY(), oldY, pig).setDuration(FENCE_FIX_ANIMATION_DURATION)
                                .setOnAnimatorEndListener(() -> {
                                    stopTask(pig);
                                    mPiggiesOccupiedPosition[pig.getIndex()] = pos;
                                    changeItemStatus(pos.y, pos.x, Item.STATE_OCCUPIED);
                                    initRunAnimation(pig, false);
                                }).start();
                    } else {
                        float[] carBodyCenterPos = new float[PIGGY_COUNT];
                        carBodyCenterPos[0] = mCarHead.getIntrinsicWidth() + mCarBody.getIntrinsicWidth() / 2;
                        for (int i = 1; i < carBodyCenterPos.length; i++) {
                            carBodyCenterPos[i] = carBodyCenterPos[i - 1] + mCarBody.getIntrinsicWidth();
                        }
                        int emptyCarriageItem = getNearestEmptyCarriage(carBodyCenterPos, x);
                        float offset = (mCarBody.getIntrinsicWidth() - pig.getWidth()) / 2;
                        float toX = carBodyCenterPos[emptyCarriageItem] - mCarBody.getIntrinsicWidth() / 2 + offset;
                        float toY = mCarBody.getY() + mCarBody.getIntrinsicHeight() - pig.getHeight();
                        mCaughtPiggiesPosition.put(emptyCarriageItem, pig.getIndex());
                        MyValueAnimator.create(pig.getX(), toX, pig.getY(), toY, pig).setDuration(FENCE_FIX_ANIMATION_DURATION).setOnAnimatorEndListener(() -> {
                            pig.setEnable(false);
                            pig.setState(Pig.STATE_STANDING);
                            int readyCount = 0;
                            for (boolean isReady : mCarriageIsOccupied) {
                                if (isReady) {
                                    readyCount++;
                                }
                            }

                            if (readyCount == mCaughtPiggies.size() && !isAllPiggiesAreReady) {
                                isAllPiggiesAreReady = true;
                                MyValueAnimator.create(mCarHead.getX(), -(mCarHead.getIntrinsicWidth() * 2 + mCarBody.getIntrinsicWidth() * 6), mCarHead.getY(), mCarHead.getY(), mCarHead)
                                        .setDuration(4000).setOnAnimatorEndListener(this::gameOver).start();
                            }
                        }).start();
                    }
                    return;
                }
                if (item.contains(x, y)) {
                    List<WayData> data = new ArrayList<>();
                    if (!ComputeWayUtil.isCurrentPositionCanSet(mItemStatus, mPiggiesOccupiedPosition, new WayData(horizontal, vertical), data)) {
                        if (data.isEmpty()) {
                            for (Pig tmp : mPiggies) {
                                if (tmp.getState() == Pig.STATE_RUNNING) {
                                    data.addAll(tmp.getPathData());
                                }
                            }
                        }
                        WayData position = ComputeWayUtil.findNextUnSelected(mItemStatus, data, new WayData(horizontal, vertical));
                        if (position != null) {
                            vertical = position.y;
                            horizontal = position.x;
                        }
                    } else if (mItemStatus[vertical][horizontal] != Item.STATE_UNSELECTED) {
                        WayData position = ComputeWayUtil.findNextUnSelected(mItemStatus, null, new WayData(horizontal, vertical));
                        if (position != null) {
                            vertical = position.y;
                            horizontal = position.x;
                        }
                    }
                    pig.setPosition(vertical, horizontal);
                    mPiggiesOccupiedPosition[pig.getIndex()] = new WayData(horizontal, vertical);
                    changeItemStatus(vertical, horizontal, Item.STATE_OCCUPIED);
                    initRunAnimation(pig, isPlayAnimation);
                    return;
                }
            }
        }
        pig.setState(Pig.STATE_STANDING);
    }

    private int getNearestEmptyCarriage(float[] carBodyCenterPos, int x) {
        int result = 0;
        float lastDistance = 0;
        for (int i = 0; i < carBodyCenterPos.length; i++) {
            if (!mCarriageIsOccupied[i]) {
                lastDistance = Math.abs(x - carBodyCenterPos[i]);
                result = i;
                break;
            }
        }
        for (int i = 1; i < carBodyCenterPos.length; i++) {
            float distance = Math.abs(x - carBodyCenterPos[i]);
            if (distance < lastDistance && !mCarriageIsOccupied[i]) {
                lastDistance = distance;
                result = i;
            }
        }
        mCarriageIsOccupied[result] = true;
        return result;
    }

    private void initCar() {
        mCarBody = new MyDrawable(0, BitmapUtil.getBitmapFromResource(getContext(), R.mipmap.ic_car_body));
        Bitmap carHead0, carHead1, carHead2, carHead3;
        carHead0 = BitmapUtil.getBitmapFromResource(getContext(), R.mipmap.ic_car_head_0);
        carHead1 = BitmapUtil.getBitmapFromResource(getContext(), R.mipmap.ic_car_head_1);
        carHead2 = BitmapUtil.getBitmapFromResource(getContext(), R.mipmap.ic_car_head_2);
        carHead3 = BitmapUtil.getBitmapFromResource(getContext(), R.mipmap.ic_car_head_3);
        mCarHead = new MyDrawable(50, carHead0, carHead1, carHead2, carHead3, carHead2, carHead1);
        mCarHead.start();
    }

    public void setCurrentLevel(int currentLevel) {
        mCurrentLevel = currentLevel;
        if (mCurrentLevel > LevelUtil.PIGSTY_MODE_MAX_LEVEL) {
            mCurrentLevel = -1;
        }
        mMissionData = LevelUtil.getMissionData(currentLevel);
        showMissionDialog();
    }

    public void release() {
        if (mPiggies != null) {
            for (Pig pig : mPiggies) {
                if (pig != null) {
                    pig.release();
                }
            }
            mPiggies = null;
        }
        if (mComputePathTasks != null) {
            for (Future task : mComputePathTasks) {
                if (task != null) {
                    task.cancel(true);
                }
            }
            mComputePathTasks = null;
        }
        if (mCarBody != null) {
            mCarBody.release();
            mCarBody = null;
        }
        if (mCarHead != null) {
            mCarHead.release();
            mCarHead = null;
        }
        if (mFrameBackgroundBitmap != null) {
            if (!mFrameBackgroundBitmap.isRecycled()) {
                mFrameBackgroundBitmap.recycle();
            }
            mFrameBackgroundBitmap = null;
        }
        mCaughtPiggies = null;
        mCaughtPiggiesPosition = null;
        mPaint = null;
        mCarriageIsOccupied = null;
        mFrameBackground = null;
        if (mPropOffsetHelper != null) {
            mPropOffsetHelper.release();
            mPropOffsetHelper = null;
        }
        mSurfaceHolder = null;
        mItems = null;
        mItemStatus = null;
        mDraggingProps = null;
        mDraggingPiggies = null;
        mDraggingPropIds = null;
        mDraggingPiggyIds = null;
        mPiggiesOccupiedPosition = null;
        mMissionDialog = null;
        mGameResultDialog = null;
        mExitDialog = null;
    }

    private void gameOver() {
        if (checkIsStandard()) {
            showVictoryDialog();
        } else {
            showFailureDialog();
        }
    }

    private boolean checkIsStandard() {
        boolean result = false;
        mValidCaughtCount = 0;
        for (boolean isOccupied : mCarriageIsOccupied) {
            if (isOccupied) {
                mValidCaughtCount++;
            }
        }
        if (mValidCaughtCount >= mMissionData.mustCaughtCount) {
            result = true;
        }
        return result;
    }

    private AlertDialog mMissionDialog, mGameResultDialog, mExitDialog, mHeartEmptyDialog;

    private void showMissionDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_pigsty_mode_mission_view, null, false);
        OnClickListener onClickListener = v -> {
            switch (v.getId()) {
                case R.id.start_button:
                    mMissionDialog.dismiss();
                    isMissionDialogShown = true;
                    Application.savePigstyModeCurrentValidHeartCount(getContext(), Application.getPigstyModeCurrentValidHeartCount(getContext()) - 1);
                    restart();
                    break;
                case R.id.menu_button:
                    ((MainActivity) getContext()).backToHome();
                    break;
                default:
                    break;
            }
        };
        ((TextView) dialogView.findViewById(R.id.message)).setText(mMissionData.toString(getContext(), mCurrentLevel));
        dialogView.findViewById(R.id.start_button).setOnClickListener(onClickListener);
        dialogView.findViewById(R.id.menu_button).setOnClickListener(onClickListener);
        mMissionDialog = new AlertDialog.Builder(getContext(), R.style.DialogTheme).setView(dialogView).setCancelable(false).show();
    }

    private void showVictoryDialog() {
        Application.saveCurrentPigstyModeLevel(getContext(), mCurrentLevel + 1);
        showGameResultDialog(false);
    }

    private void showFailureDialog() {
        showGameResultDialog(true);
    }

    private void showGameResultDialog(boolean isRequestHelp) {
        String message = isRequestHelp ? String.format(Locale.getDefault(), getContext().getString(R.string.pigsty_mode_lose_message_format), mValidCaughtCount, mMissionData.mustCaughtCount)
                : String.format(Locale.getDefault(), getContext().getString(R.string.pigsty_mode_won_message_format),
                (SystemClock.uptimeMillis() - mStartTime) / 1000, mValidCaughtCount, mPropOffsetHelper.getLeavedPropCount());
        OnClickListener onClickListener = v -> {
            String shareMessage = String.format(v.getContext().getString(R.string.pigsty_mode_share_won_format), mCurrentLevel, mPropOffsetHelper.getLeavedPropCount(), mValidCaughtCount);
            switch (v.getId()) {
                case R.id.ic_share_to_wechat:
                    ShareUtil.shareToWeChat(v.getContext(), isRequestHelp, shareMessage);
                    break;
                case R.id.ic_share_to_moments:
                    ShareUtil.shareToWeChatMoments(v.getContext(), isRequestHelp, shareMessage);
                    break;
                case R.id.ic_share_to_qq:
                    ShareUtil.shareToQQ(v.getContext(), isRequestHelp, shareMessage);
                    break;
                case R.id.ic_share_to_qzone:
                    ShareUtil.shareToQZone(v.getContext(), isRequestHelp, shareMessage);
                    break;
                case R.id.positive_button:
                    mGameResultDialog.dismiss();
                    if (!isRequestHelp) {
                        if (mCurrentLevel > 0) {
                            mCurrentLevel++;
                        }
                    }
                    if (checkHeartIsEnough()) {
                        ((MainActivity) getContext()).reloadFixPigstyMode(mCurrentLevel);
                    } else {
                        showHeartIsEmptyDialog();
                    }
                    break;
                case R.id.negative_button:
                    if (isRequestHelp) {
                        ((MainActivity) getContext()).backToHome();
                    } else {
                        mGameResultDialog.dismiss();
                        if (checkHeartIsEnough()) {
                            ((MainActivity) getContext()).reloadFixPigstyMode(mCurrentLevel);
                        } else {
                            showHeartIsEmptyDialog();
                        }
                    }
                    break;
                default:
                    break;
            }
        };
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_game_over_view, null, false);
        ((TextView) dialogView.findViewById(R.id.message)).setText(message);
        ((TextView) dialogView.findViewById(R.id.action_text)).setText(isRequestHelp ? R.string.request_help : R.string.share_achievements);
        ((TextView) dialogView.findViewById(R.id.positive_button)).setText(isRequestHelp ? R.string.again : R.string.next);
        ((TextView) dialogView.findViewById(R.id.negative_button)).setText(isRequestHelp ? R.string.menu : R.string.again);
        dialogView.findViewById(R.id.ic_share_to_wechat).setOnClickListener(onClickListener);
        dialogView.findViewById(R.id.ic_share_to_moments).setOnClickListener(onClickListener);
        dialogView.findViewById(R.id.ic_share_to_qq).setOnClickListener(onClickListener);
        dialogView.findViewById(R.id.ic_share_to_qzone).setOnClickListener(onClickListener);
        dialogView.findViewById(R.id.positive_button).setOnClickListener(onClickListener);
        dialogView.findViewById(R.id.negative_button).setOnClickListener(onClickListener);
        //main thread
        post(() -> mGameResultDialog = new AlertDialog.Builder(getContext(), R.style.DialogTheme).setView(dialogView).setCancelable(false).show());
    }

    private void showHeartIsEmptyDialog() {
        //main thread
        post(() -> {
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_heart_is_empty_view, null, false);
            dialogView.findViewById(R.id.menu_button).setOnClickListener(v -> ((MainActivity) getContext()).backToHome());
            mHeartEmptyDialog = new AlertDialog.Builder(getContext(), R.style.DialogTheme).setView(dialogView).setCancelable(false).show();
        });
    }

    private boolean checkHeartIsEnough() {
        return Application.getPigstyModeCurrentValidHeartCount(getContext()) > 0;
    }

    private void showExitDialog(OnExitedListener listener) {
        if (mGameResultDialog != null && mGameResultDialog.isShowing()) {
            mGameResultDialog.dismiss();
            exitNow();
            listener.onExited();
        } else if (mMissionDialog != null && mMissionDialog.isShowing()) {
            mMissionDialog.dismiss();
            exitNow();
            listener.onExited();
        } else if (mHeartEmptyDialog != null && mHeartEmptyDialog.isShowing()) {
            mHeartEmptyDialog.dismiss();
            exitNow();
            listener.onExited();
        } else {
            if (mExitDialog == null) {
                initExitDialog(listener);
            }
            if (!mExitDialog.isShowing()) {
                mExitDialog.show();
            }
        }
    }

    private void initExitDialog(OnExitedListener listener) {
        OnClickListener onClickListener = v -> {
            switch (v.getId()) {
                case R.id.continue_game_btn:
                    mExitDialog.dismiss();
                    break;
                case R.id.back_to_menu_btn:
                    mExitDialog.dismiss();
                    exitNow();
                    listener.onExited();
                    break;
                default:
                    break;
            }
        };
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_exit_view, null, false);
        dialogView.findViewById(R.id.continue_game_btn).setOnClickListener(onClickListener);
        dialogView.findViewById(R.id.back_to_menu_btn).setOnClickListener(onClickListener);
        mExitDialog = new AlertDialog.Builder(getContext(), R.style.DialogTheme).setView(dialogView).create();
    }

    public void exitNow() {
        //触发surfaceDestroyed
        setVisibility(GONE);
        release();
    }

    public void exit(OnExitedListener listener) {
        showExitDialog(listener);
    }

    public interface OnExitedListener {
        void onExited();
    }
}
