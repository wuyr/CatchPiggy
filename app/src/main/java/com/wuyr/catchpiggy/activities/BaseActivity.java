package com.wuyr.catchpiggy.activities;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wuyr on 18-1-23 上午9:48.
 */

public abstract class BaseActivity extends AppCompatActivity {

    private static Map<String, BaseActivity> mAliveActivities;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mAliveActivities == null) {
            mAliveActivities = new HashMap<>();
        }
        mAliveActivities.put(this.getClass().getSimpleName(), this);

        setContentView(getLayoutId());
        mode2();
        initView();
    }

    private void mode2() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            Window window = getWindow();
            // Translucent status bar
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // Translucent navigation bar
//            window.setFlags(
//                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
//                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
//                            | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                            | View.SYSTEM_UI_FLAG_IMMERSIVE
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.setStatusBarColor(Color.TRANSPARENT);
                window.setNavigationBarColor(Color.TRANSPARENT);
            }
        }
    }

    public void back(View view) {
        finish();
    }

    protected abstract int getLayoutId();

    protected abstract void initView();

    protected abstract boolean isStatusBarNeedImmerse();

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (mOnRequestPermissionsResultListener != null) {
            if (permissions.length != grantResults.length) {
                mOnRequestPermissionsResultListener.onFailure();
            } else {
                if (checkIsAllPermissionAgreed(grantResults)) {
                    mOnRequestPermissionsResultListener.onSuccess();
                } else {
                    mOnRequestPermissionsResultListener.onFailure();
                }
            }
        }
    }

    private boolean checkIsAllPermissionAgreed(int[] permissions) {
        boolean isAllPermissionAgreed = true;
        if (permissions.length == 0) {
            isAllPermissionAgreed = false;
        } else {
            for (int tmp : permissions) {
                if (tmp != PackageManager.PERMISSION_GRANTED) {
                    isAllPermissionAgreed = false;
                    break;
                }
            }
        }
        return isAllPermissionAgreed;
    }

    private OnRequestPermissionsResultListener mOnRequestPermissionsResultListener;

    public void verifyPermissions(OnRequestPermissionsResultListener listener, String... permissions) {
        mOnRequestPermissionsResultListener = listener;
        if (checkPermissionGroup(permissions)) {
            listener.onSuccess();
        } else {
            ActivityCompat.requestPermissions(this, permissions, 0);
        }
    }

    private boolean checkPermissionGroup(String... permissions) {
        boolean isHasAllPermissions = true;
        for (String tmp : permissions) {
            if (ActivityCompat.checkSelfPermission(this, tmp) != PackageManager.PERMISSION_GRANTED) {
                isHasAllPermissions = false;
                break;
            }
        }
        return isHasAllPermissions;
    }

    protected boolean isNeedRequestPermissions() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    @IntDef({MODE_LIGHT, MODE_DARK})
    @Retention(RetentionPolicy.SOURCE)
    private @interface Mode {
    }

    protected static final int MODE_LIGHT = 1280, MODE_DARK = 9216;

    protected void setStatusBarTextColor(@Mode int mode) {
        if (isNeedRequestPermissions()) {
            getWindow().getDecorView().setSystemUiVisibility(mode);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAliveActivities != null) {
            mAliveActivities.remove(this.getClass().getSimpleName());
            if (mAliveActivities.isEmpty()) {
                mAliveActivities = null;
            }
        }
    }

    public interface OnRequestPermissionsResultListener {
        void onSuccess();

        void onFailure();
    }
}
