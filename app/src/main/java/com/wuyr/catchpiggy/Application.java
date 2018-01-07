package com.wuyr.catchpiggy;

import android.content.Context;
import android.content.SharedPreferences;

import com.tencent.bugly.crashreport.CrashReport;

/**
 * Created by wuyr on 17-10-23 下午1:14.
 */

public class Application extends com.mob.MobApplication {

    private static final String NAME = Application.class.getSimpleName();
    //经典模式 新手指引对话框
    public static final String CLASSIC_MODE_GUIDE_DIALOG_SHOWN = "ClassicModeGuideDialogShown";
    //修猪圈模式 新手指引对话框
    public static final String PIGSTY_MODE_GUIDE_DIALOG_SHOWN = "PigstyModeGuideDialogShown";
    //经典模式当前关卡
    private static final String CURRENT_CLASSIC_MODE_LEVEL = "CurrentClassicModeLevel";
    //修猪圈模式当前关卡
    private static final String CURRENT_PIGSTY_MODE_LEVEL = "CurrentPigstyModeLevel";
    //修猪圈模式当前心数量
    private static final String PIGSTY_MODE_CURRENT_VALID_HEART_COUNT = "PigstyModeCurrentValidHeartCount";
    //经典模式当前心数量
    private static final String CLASSIC_MODE_CURRENT_VALID_DRAG_COUNT = "ClassicModeCurrentValidDragCount";
    //经典模式当前心数量
    private static final String CLASSIC_MODE_CURRENT_VALID_NAVIGATION_COUNT = "ClassicModeCurrentValidNavigationCount";
    //经典模式当前心数量
    private static final String CLASSIC_MODE_CURRENT_VALID_HEART_COUNT = "ClassicModeCurrentValidHeartCount";
    //修猪圈模式上次心更新时间
    private static final String PIGSTY_MODE_HEART_LAST_UPDATE_TIME = "PigstyModeHeartLastUpdateTime";
    //经典模式上次心更新时间
    private static final String CLASSIC_MODE_HEART_LAST_UPDATE_TIME = "ClassicModeHeartLastUpdateTime";
    //经典模式-导航 上次保存时间
    private static final String CLASSIC_MODE_NAVIGATION_LAST_UPDATE_TIME = "ClassicModeNavigationLastUpdateTime";
    //经典模式-拖动 上次保存时间
    private static final String CLASSIC_MODE_DRAG_LAST_UPDATE_TIME = "ClassicModeDragLastUpdateTime";
    //心 最大数
    private static final long MAX_HEART_COUNT = 5L;
    //心 刷新时长 5分钟
    private static final long HEART_RECOVER_DURATION = 300000L;
    //经典模式-导航 最大数
    private static final long MAX_NAVIGATION_COUNT = 5L;
    //经典模式-导航 刷新时长 1天
    private static final long NAVIGATION_RECOVER_DURATION = 86400000L;
    //经典模式-拖动 最大数
    private static final long MAX_DRAG_COUNT = 3L;
    //经典模式-拖动 刷新时长 2天
    private static final long DRAG_RECOVER_DURATION = 172800000L;

    @Override
    public void onCreate() {
        super.onCreate();
        //init bugly
        CrashReport.initCrashReport(this, "488e87756b", false);
    }

    @Override
    protected String getAppkey() {
        return "23557c69a0de4";
    }

    @Override
    protected String getAppSecret() {
        return "1b92a94a1fa59ccf0c9a58865c928d7c";
    }

    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(NAME, MODE_PRIVATE);
    }

    /**
     * 获取经典模式当前关卡
     */
    public static int getCurrentClassicModeLevel(Context context) {
        return (int) getSharedPreferences(context).getLong(CURRENT_CLASSIC_MODE_LEVEL, 1);
    }

    /**
     * 保存经典模式当前关卡
     */
    public static void saveCurrentClassicModeLevel(Context context, long currentLevel) {
        getSharedPreferences(context).edit().putLong(CURRENT_CLASSIC_MODE_LEVEL, currentLevel).apply();
    }

    /**
     * 获取修猪圈模式当前关卡
     */
    public static int getCurrentPigstyModeLevel(Context context) {
        return (int) getSharedPreferences(context).getLong(CURRENT_PIGSTY_MODE_LEVEL, 1);
    }

    /**
     * 保存修猪圈模式当前关卡
     */
    public static void saveCurrentPigstyModeLevel(Context context, long currentLevel) {
        getSharedPreferences(context).edit().putLong(CURRENT_PIGSTY_MODE_LEVEL, currentLevel).apply();
    }

    /**
     * 获取修猪圈模式当前可用心数量
     */
    public static int getPigstyModeCurrentValidHeartCount(Context context) {
        updatePigstyModeValidHeartCount(context);
        return (int) getSharedPreferences(context).getLong(PIGSTY_MODE_CURRENT_VALID_HEART_COUNT, MAX_HEART_COUNT);
    }

    /**
     * 保存修猪圈模式当前可用心数量
     */
    public static void savePigstyModeCurrentValidHeartCount(Context context, long count) {
        getSharedPreferences(context).edit().putLong(PIGSTY_MODE_CURRENT_VALID_HEART_COUNT, count).apply();
        updatePigstyModeHeartLastSaveTime(context);
    }

    /**
     * 获取经典模式当前可用心数量
     */
    public static int getClassicModeCurrentValidHeartCount(Context context) {
        updateClassicModeValidHeartCount(context);
        return (int) getSharedPreferences(context).getLong(CLASSIC_MODE_CURRENT_VALID_HEART_COUNT, MAX_HEART_COUNT);
    }

    /**
     * 保存经典模式可用心数量
     */
    public static void saveClassicModeCurrentValidHeartCount(Context context, long count) {
        getSharedPreferences(context).edit().putLong(CLASSIC_MODE_CURRENT_VALID_HEART_COUNT, count).apply();
        updateClassicModeHeartLastSaveTime(context);
    }

    /**
     * 获取 经典模式-导航 当前可用数量
     */
    public static int getClassicModeCurrentValidNavigationCount(Context context) {
        updateClassicModeValidNavigationCount(context);
        return (int) getSharedPreferences(context).getLong(CLASSIC_MODE_CURRENT_VALID_NAVIGATION_COUNT, MAX_NAVIGATION_COUNT);
    }

    /**
     * 保存 经典模式-导航 可用数量
     */
    public static void saveClassicModeCurrentValidNavigationCount(Context context, long count) {
        getSharedPreferences(context).edit().putLong(CLASSIC_MODE_CURRENT_VALID_NAVIGATION_COUNT, count).apply();
        updateClassicModeNavigationLastSaveTime(context);
    }

    /**
     * 获取 经典模式-导航 当前可用数量
     */
    public static int getClassicModeCurrentValidDragCount(Context context) {
        updateClassicModeValidDragCount(context);
        return (int) getSharedPreferences(context).getLong(CLASSIC_MODE_CURRENT_VALID_DRAG_COUNT, MAX_DRAG_COUNT);
    }

    /**
     * 保存 经典模式-导航 可用数量
     */
    public static void saveClassicModeCurrentValidDragCount(Context context, long count) {
        getSharedPreferences(context).edit().putLong(CLASSIC_MODE_CURRENT_VALID_DRAG_COUNT, count).apply();
        updateClassicModeDragLastSaveTime(context);
    }

    /**
     * 刷新修猪圈模式上次心更新时间
     */
    private static void updatePigstyModeHeartLastSaveTime(Context context) {
        getSharedPreferences(context).edit().putLong(PIGSTY_MODE_HEART_LAST_UPDATE_TIME, System.currentTimeMillis()).apply();
    }

    /**
     * 刷新经典模式上次心更新时间
     */
    private static void updateClassicModeHeartLastSaveTime(Context context) {
        getSharedPreferences(context).edit().putLong(CLASSIC_MODE_HEART_LAST_UPDATE_TIME, System.currentTimeMillis()).apply();
    }

    /**
     * 刷新 经典模式-导航 上次保存时间
     */
    private static void updateClassicModeNavigationLastSaveTime(Context context) {
        getSharedPreferences(context).edit().putLong(CLASSIC_MODE_NAVIGATION_LAST_UPDATE_TIME, System.currentTimeMillis()).apply();
    }

    /**
     * 刷新 经典模式-拖动 上次保存时间
     */
    private static void updateClassicModeDragLastSaveTime(Context context) {
        getSharedPreferences(context).edit().putLong(CLASSIC_MODE_DRAG_LAST_UPDATE_TIME, System.currentTimeMillis()).apply();
    }

    /**
     * 刷新经典模式可用心数量
     */
    private static void updateClassicModeValidHeartCount(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        if (sharedPreferences.getLong(CLASSIC_MODE_CURRENT_VALID_HEART_COUNT, MAX_HEART_COUNT) < MAX_HEART_COUNT) {
            long intervalTime = System.currentTimeMillis() - sharedPreferences.getLong(CLASSIC_MODE_HEART_LAST_UPDATE_TIME, 0);
            if (intervalTime > HEART_RECOVER_DURATION) {
                long recoverHeartCount = intervalTime / HEART_RECOVER_DURATION;
                if (recoverHeartCount > 0) {
                    recoverHeartCount += sharedPreferences.getLong(CLASSIC_MODE_CURRENT_VALID_HEART_COUNT, MAX_HEART_COUNT);
                    if (recoverHeartCount > MAX_HEART_COUNT) {
                        recoverHeartCount = MAX_HEART_COUNT;
                    }
                    saveClassicModeCurrentValidHeartCount(context, recoverHeartCount);
                }
            }
        }
    }

    /**
     * 刷新修猪圈可用心数量
     */
    private static void updatePigstyModeValidHeartCount(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        if (sharedPreferences.getLong(PIGSTY_MODE_CURRENT_VALID_HEART_COUNT, MAX_HEART_COUNT) < MAX_HEART_COUNT) {
            long intervalTime = System.currentTimeMillis() - sharedPreferences.getLong(PIGSTY_MODE_HEART_LAST_UPDATE_TIME, 0);
            if (intervalTime > HEART_RECOVER_DURATION) {
                long recoverHeartCount = intervalTime / HEART_RECOVER_DURATION;
                if (recoverHeartCount > 0) {
                    recoverHeartCount += sharedPreferences.getLong(PIGSTY_MODE_CURRENT_VALID_HEART_COUNT, MAX_HEART_COUNT);
                    if (recoverHeartCount > MAX_HEART_COUNT) {
                        recoverHeartCount = MAX_HEART_COUNT;
                    }
                    savePigstyModeCurrentValidHeartCount(context, recoverHeartCount);
                }
            }
        }
    }

    /**
     * 刷新 经典模式-导航 可用数量
     */
    private static void updateClassicModeValidNavigationCount(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        if (sharedPreferences.getLong(CLASSIC_MODE_CURRENT_VALID_NAVIGATION_COUNT, MAX_NAVIGATION_COUNT) < MAX_NAVIGATION_COUNT) {
            long intervalTime = System.currentTimeMillis() - sharedPreferences.getLong(CLASSIC_MODE_NAVIGATION_LAST_UPDATE_TIME, 0);
            if (intervalTime > NAVIGATION_RECOVER_DURATION) {
                long recoverNavigationCount = intervalTime / NAVIGATION_RECOVER_DURATION;
                if (recoverNavigationCount > 0) {
                    recoverNavigationCount += sharedPreferences.getLong(CLASSIC_MODE_CURRENT_VALID_NAVIGATION_COUNT, MAX_NAVIGATION_COUNT);
                    if (recoverNavigationCount > MAX_NAVIGATION_COUNT) {
                        recoverNavigationCount = MAX_NAVIGATION_COUNT;
                    }
                    saveClassicModeCurrentValidNavigationCount(context, recoverNavigationCount);
                }
            }
        }
    }

    /**
     * 刷新 经典模式-拖动 可用数量
     */
    private static void updateClassicModeValidDragCount(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        if (sharedPreferences.getLong(CLASSIC_MODE_CURRENT_VALID_DRAG_COUNT, MAX_DRAG_COUNT) < MAX_DRAG_COUNT) {
            long intervalTime = System.currentTimeMillis() - sharedPreferences.getLong(CLASSIC_MODE_DRAG_LAST_UPDATE_TIME, 0);
            if (intervalTime > DRAG_RECOVER_DURATION) {
                long recoverDragCount = intervalTime / DRAG_RECOVER_DURATION;
                if (recoverDragCount > 0) {
                    recoverDragCount += sharedPreferences.getLong(CLASSIC_MODE_CURRENT_VALID_DRAG_COUNT, MAX_DRAG_COUNT);
                    if (recoverDragCount > MAX_DRAG_COUNT) {
                        recoverDragCount = MAX_DRAG_COUNT;
                    }
                    saveClassicModeCurrentValidDragCount(context, recoverDragCount);
                }
            }
        }
    }
}
