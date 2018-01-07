package com.wuyr.catchpiggy.customize.views;

import android.content.Context;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.wuyr.catchpiggy.Application;
import com.wuyr.catchpiggy.R;
import com.wuyr.catchpiggy.activities.MainActivity;
import com.wuyr.catchpiggy.utils.LevelUtil;
import com.wuyr.catchpiggy.utils.ShareUtil;

import java.util.Locale;

/**
 * Created by wuyr on 17-12-25 上午1:44.
 */

public class ClassicModeView extends FrameLayout {

    private String mLevelStringFormat;
    private ClassicMode mClassicMode;
    private TextView mRefreshButton;
    private TextView mLevelTextView;
    private long mStartTime;
    private int mCurrentLevel;

    public ClassicModeView(@NonNull Context context) {
        this(context, null);
    }

    public ClassicModeView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClassicModeView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_classic_mode, this, true);
        mClassicMode = findViewById(R.id.item_group);
        mClassicMode.setOnOverListener(new ClassicMode.OnGameOverListener() {
            @Override
            public void onWin() {
                showVictoryDialog();
            }

            @Override
            public void onLost() {
                showFailureDialog();
            }
        });
        mClassicMode.setOnPiggyDraggedListener((() -> findViewById(R.id.drag_btn)
                .setBackgroundResource(checkDragCountIsEnough(false) ? R.mipmap.ic_drag : R.mipmap.ic_drag_disable)));
        OnClickListener onClickListener = v -> {
            switch (v.getId()) {
                case R.id.guide_btn:
                    if (checkNavigationCountIsEnough()) {
                        mClassicMode.setNavigationOn();
                        v.setBackgroundResource(R.mipmap.ic_navigation_press);
                        v.setEnabled(false);
                    } else {
                        disableNavigationButton(v);
                    }
                    int navigationCount = Application.getClassicModeCurrentValidNavigationCount(getContext());
                    ((TextView) v).setText(String.valueOf(navigationCount));
                    break;
                case R.id.drag_btn:
                    if (checkDragCountIsEnough(true)) {
                        mClassicMode.setDragEnable();
                        v.setBackgroundResource(R.mipmap.ic_drag_press);
                        v.setEnabled(false);
                    } else {
                        disableDragButton(v);
                    }
                    int dragCount = Application.getClassicModeCurrentValidDragCount(getContext());
                    ((TextView) v).setText(String.valueOf(dragCount));
                    break;
                case R.id.refresh_btn:
                    resetStatus();
                    mClassicMode.setLevel(mCurrentLevel);
                    mStartTime = SystemClock.uptimeMillis();
                    if (mCurrentLevel > 0) {
                        mLevelTextView.setText(String.format(mLevelStringFormat, mCurrentLevel));
                    }
                    break;
                case R.id.undo_btn:
                    try {
                        int count = Integer.parseInt(((TextView) v).getText().toString());
                        if (count == 1) {
                            disableUndoButton(v);
                        } else {
                            count--;
                            ((TextView) v).setText(String.valueOf(count));
                        }
                        mClassicMode.undo();
                    } catch (NumberFormatException e) {
                        disableUndoButton(v);
                    }
                    break;
                default:
                    break;
            }
        };
        mRefreshButton = findViewById(R.id.refresh_btn);
        View undoButton = findViewById(R.id.undo_btn);
        mRefreshButton.setOnClickListener(onClickListener);
        undoButton.setOnClickListener(onClickListener);
        mLevelStringFormat = getContext().getString(R.string.level_format);
        mLevelTextView = findViewById(R.id.level_text);
        findViewById(R.id.guide_btn).setOnClickListener(onClickListener);
        findViewById(R.id.drag_btn).setOnClickListener(onClickListener);
    }

    private void disableUndoButton(View v) {
        v.setBackgroundResource(R.mipmap.ic_undo_disable);
        v.setEnabled(false);
        ((TextView) v).setText("0");
    }

    private void disableNavigationButton(View v) {
        v.setBackgroundResource(R.mipmap.ic_navigation_disable);
        v.setEnabled(false);
        ((TextView) v).setText("0");
    }

    private void disableDragButton(View v) {
        v.setBackgroundResource(R.mipmap.ic_drag_disable);
        v.setEnabled(false);
        ((TextView) v).setText("0");
    }

    private void resetStatus() {
        if (checkHeartIsEnough(true)) {
            TextView undoBtn = findViewById(R.id.undo_btn);
            undoBtn.setEnabled(true);
            undoBtn.setBackgroundResource(R.mipmap.ic_undo);
            undoBtn.setText("3");

            TextView guideButton = findViewById(R.id.guide_btn);
            int navigationCount = Application.getClassicModeCurrentValidNavigationCount(getContext());
            if (navigationCount == 0) {
                disableNavigationButton(guideButton);
            } else {
                guideButton.setEnabled(true);
                guideButton.setBackgroundResource(R.mipmap.ic_navigation);
                guideButton.setText(String.valueOf(navigationCount));
            }

            TextView dragButton = findViewById(R.id.drag_btn);
            int dragCount = Application.getClassicModeCurrentValidDragCount(getContext());
            if (dragCount == 0) {
                disableDragButton(dragButton);
            } else {
                dragButton.setEnabled(true);
                dragButton.setBackgroundResource(R.mipmap.ic_drag);
                dragButton.setText(String.valueOf(dragCount));
            }

            int heartCount = Application.getClassicModeCurrentValidHeartCount(getContext());
            mRefreshButton.setText(String.valueOf(heartCount));
            if (heartCount == 0) {
                mRefreshButton.setEnabled(false);
                mRefreshButton.setBackgroundResource(R.mipmap.ic_refresh_disable);
            } else {
                mRefreshButton.setEnabled(true);
                mRefreshButton.setBackgroundResource(R.mipmap.ic_refresh);
            }
        }
    }

    public void setCurrentLevel(int currentLevel) {
        mCurrentLevel = currentLevel;
        if (mCurrentLevel > LevelUtil.CLASSIC_MODE_MAX_LEVEL) {
            mCurrentLevel = -1;
        }
        mRefreshButton.performClick();
    }

    public void release() {
        mClassicMode.release();
        mGameResultDialog = null;
        mExitDialog = null;
    }

    private AlertDialog mGameResultDialog, mExitDialog, mHeartEmptyDialog;

    private void showVictoryDialog() {
        Application.saveCurrentClassicModeLevel(getContext(), mCurrentLevel + 1);
        initGameResultDialog(false);
        mGameResultDialog.show();
    }

    private void showFailureDialog() {
        initGameResultDialog(true);
        mGameResultDialog.show();
    }

    private void initGameResultDialog(boolean isRequestHelp) {
        String message = isRequestHelp ? getContext().getString(R.string.classic_mode_lose_message_format)
                : String.format(Locale.getDefault(), getContext().getString(R.string.classic_mode_won_message_format),
                (SystemClock.uptimeMillis() - mStartTime) / 1000, mClassicMode.getHistorySize());
        OnClickListener onClickListener = v -> {
            String shareMessage = String.format(v.getContext().getString(R.string.classic_mode_share_won_format), mCurrentLevel, mClassicMode.getHistorySize());
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
                    if (isRequestHelp) {
                        if (checkHeartIsEnough(false)) {
                            mRefreshButton.performClick();
                        } else {
                            showHeartIsEmptyDialog();
                        }
                    } else {
                        if (mCurrentLevel > 0) {
                            mCurrentLevel++;
                        }
                        if (checkHeartIsEnough(false)) {
                            mRefreshButton.performClick();
                        } else {
                            showHeartIsEmptyDialog();
                        }
                    }
                    break;
                case R.id.negative_button:
                    if (isRequestHelp) {
                        ((MainActivity) getContext()).backToHome();
                    } else {
                        mGameResultDialog.dismiss();
                        if (checkHeartIsEnough(false)) {
                            mRefreshButton.performClick();
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
        mGameResultDialog = new AlertDialog.Builder(getContext(), R.style.DialogTheme).setView(dialogView).setCancelable(false).create();
    }

    private void showHeartIsEmptyDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_heart_is_empty_view, null, false);
        dialogView.findViewById(R.id.menu_button).setOnClickListener(v -> ((MainActivity) getContext()).backToHome());
        mHeartEmptyDialog = new AlertDialog.Builder(getContext(), R.style.DialogTheme).setView(dialogView).setCancelable(false).show();
    }

    private boolean checkHeartIsEnough(boolean isUpdateCount) {
        boolean isEnough = Application.getClassicModeCurrentValidHeartCount(getContext()) > 0;
        if (isUpdateCount && isEnough) {
            Application.saveClassicModeCurrentValidHeartCount(getContext(), Application.getClassicModeCurrentValidHeartCount(getContext()) - 1);
        }
        return isEnough;
    }

    private boolean checkNavigationCountIsEnough() {
        boolean isEnough = Application.getClassicModeCurrentValidNavigationCount(getContext()) > 0;
        if (isEnough) {
            Application.saveClassicModeCurrentValidNavigationCount(getContext(), Application.getClassicModeCurrentValidNavigationCount(getContext()) - 1);
        }
        return isEnough;
    }

    private boolean checkDragCountIsEnough(boolean isUpdateCount) {
        boolean isEnough = Application.getClassicModeCurrentValidDragCount(getContext()) > 0;
        if (isUpdateCount && isEnough) {
            Application.saveClassicModeCurrentValidDragCount(getContext(), Application.getClassicModeCurrentValidDragCount(getContext()) - 1);
        }
        return isEnough;
    }

    private void showExitDialog(PigstyMode.OnExitedListener listener) {
        if (mGameResultDialog != null && mGameResultDialog.isShowing()) {
            mGameResultDialog.dismiss();
            release();
            listener.onExited();
        } else if (mHeartEmptyDialog != null && mHeartEmptyDialog.isShowing()) {
            mHeartEmptyDialog.dismiss();
            release();
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

    private void initExitDialog(PigstyMode.OnExitedListener listener) {
        OnClickListener onClickListener = v -> {
            switch (v.getId()) {
                case R.id.continue_game_btn:
                    mExitDialog.dismiss();
                    break;
                case R.id.back_to_menu_btn:
                    mExitDialog.dismiss();
                    release();
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

    public void exit(PigstyMode.OnExitedListener listener) {
        showExitDialog(listener);
    }
}
