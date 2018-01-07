package com.wuyr.catchpiggy.customize.views;

import android.app.Dialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wuyr.catchpiggy.Application;
import com.wuyr.catchpiggy.R;
import com.wuyr.catchpiggy.activities.MainActivity;
import com.wuyr.catchpiggy.utils.LevelUtil;

import java.io.IOException;

/**
 * Created by wuyr on 18-1-5 上午12:38.
 */

public class LevelSelectView extends LinearLayout {

    private HeartView mHeartView;
    private LevelSelect mLevelSelect;
    private boolean isClassicMode, isGuideDialogShown;
    private Bitmap[] mGuideImages;
    private int mDialogCurrentPageIndex;

    public LevelSelectView(Context context) {
        this(context, null);
    }

    public LevelSelectView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LevelSelectView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_level_select, this, true);
        mHeartView = findViewById(R.id.heart_view);
        mLevelSelect = findViewById(R.id.level_select);
    }

    public void setValidHeartCount(int count) {
        mHeartView.setValidHeartCount(count);
    }

    public void setMaxLevelCount(int count) {
        mLevelSelect.setMaxItemCount(count);
        isClassicMode = count == LevelUtil.CLASSIC_MODE_MAX_LEVEL + 1;
        initGuideDialogData();
    }

    public void initGuideDialogData() {
        isGuideDialogShown = Application.getSharedPreferences(getContext()).getBoolean(
                isClassicMode ? Application.CLASSIC_MODE_GUIDE_DIALOG_SHOWN : Application.PIGSTY_MODE_GUIDE_DIALOG_SHOWN, false);
        if (!isGuideDialogShown) {
            mGuideImages = new Bitmap[3];
            AssetManager assetManager = getContext().getAssets();
            try {
                if (isClassicMode) {
                    mGuideImages[0] = BitmapFactory.decodeStream(assetManager.open("ic_guide_c1.jpg"));
                    mGuideImages[1] = BitmapFactory.decodeStream(assetManager.open("ic_guide_c2.jpg"));
                    mGuideImages[2] = BitmapFactory.decodeStream(assetManager.open("ic_guide_c3.jpg"));
                } else {
                    mGuideImages[0] = BitmapFactory.decodeStream(assetManager.open("ic_guide_p1.png"));
                    mGuideImages[1] = BitmapFactory.decodeStream(assetManager.open("ic_guide_p2.jpg"));
                    mGuideImages[2] = BitmapFactory.decodeStream(assetManager.open("ic_guide_p3.jpg"));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setValidLevelCount(int count) {
        mLevelSelect.setValidItemCount(count);
    }

    public void setOnLevelSelectedListener(LevelSelect.OnLevelSelectedListener levelSelectedListener) {
        mLevelSelect.setOnLevelSelectedListener(level -> {
            if (!isGuideDialogShown) {
                isGuideDialogShown = true;
                showGuideDialog();
                return;
            }
            if (checkHeartIsEnough()) {
                levelSelectedListener.onSelected(level);
            } else {
                showHeartIsEmptyDialog();
            }
        });
    }

    private Dialog mHeartEmptyDialog;

    private void showHeartIsEmptyDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_heart_is_empty_view, null, false);
        dialogView.findViewById(R.id.menu_button).setOnClickListener(v -> {
            mHeartEmptyDialog.dismiss();
            ((MainActivity) getContext()).backToHome();
        });
        if (mHeartEmptyDialog == null) {
            mHeartEmptyDialog = new AlertDialog.Builder(getContext(), R.style.DialogTheme).setView(dialogView).setCancelable(false).show();
        }
    }

    private boolean checkHeartIsEnough() {
        return (isClassicMode ?
                Application.getClassicModeCurrentValidHeartCount(getContext())
                : Application.getPigstyModeCurrentValidHeartCount(getContext())) > 0;
    }

    private Dialog mGuideDialog;
    private View mDialogView;

    private void showGuideDialog() {
        mDialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_guide_view, null, false);
        OnClickListener onClickListener = v -> {
            switch (v.getId()) {
                case R.id.previous:
                    if (mDialogCurrentPageIndex > 0) {
                        mDialogCurrentPageIndex--;
                        if (mGuideImages != null) {
                            ((ImageView) mDialogView.findViewById(R.id.image_view)).setImageBitmap(mGuideImages[mDialogCurrentPageIndex]);
                        }
                    }
                    break;
                case R.id.next:
                    if (mDialogCurrentPageIndex < 2) {
                        mDialogCurrentPageIndex++;
                        if (mGuideImages != null) {
                            ((ImageView) mDialogView.findViewById(R.id.image_view)).setImageBitmap(mGuideImages[mDialogCurrentPageIndex]);
                        }
                    } else {
                        mGuideDialog.dismiss();
                        Application.getSharedPreferences(getContext()).edit().putBoolean(isClassicMode ?
                                Application.CLASSIC_MODE_GUIDE_DIALOG_SHOWN : Application.PIGSTY_MODE_GUIDE_DIALOG_SHOWN, true).apply();
                        ((ImageView) mDialogView.findViewById(R.id.image_view)).setImageBitmap(null);
                        mDialogView = null;
                        if (mGuideImages != null) {
                            for (Bitmap bitmap : mGuideImages) {
                                if (bitmap != null && !bitmap.isRecycled()) {
                                    bitmap.recycle();
                                }
                            }
                            mGuideImages = null;
                        }
                    }
                    break;
                default:
                    break;
            }
            if (mDialogView != null) {
                ((TextView) mDialogView.findViewById(R.id.next)).setText(mDialogCurrentPageIndex == 2 ? R.string.close : R.string.next_page);
            }
        };
        mDialogView.findViewById(R.id.previous).setOnClickListener(onClickListener);
        mDialogView.findViewById(R.id.next).setOnClickListener(onClickListener);
        if (mGuideImages != null) {
            ((ImageView) mDialogView.findViewById(R.id.image_view)).setImageBitmap(mGuideImages[0]);
        }
        mGuideDialog = new AlertDialog.Builder(getContext(), R.style.DialogTheme).setView(mDialogView).setCancelable(false).show();
    }

    public void release() {
        if (mHeartView != null) {
            mHeartView.release();
            mHeartView = null;
        }
        if (mLevelSelect != null) {
            mLevelSelect.release();
            mLevelSelect = null;
        }
        mHeartEmptyDialog = null;
        mGuideDialog = null;
    }
}
