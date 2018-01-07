package com.wuyr.catchpiggy.activities;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.FrameLayout;

import com.wuyr.catchpiggy.Application;
import com.wuyr.catchpiggy.R;
import com.wuyr.catchpiggy.customize.views.ClassicModeView;
import com.wuyr.catchpiggy.customize.views.HomeView;
import com.wuyr.catchpiggy.customize.views.LevelSelectView;
import com.wuyr.catchpiggy.customize.views.LoadingView;
import com.wuyr.catchpiggy.customize.views.PigstyMode;
import com.wuyr.catchpiggy.utils.LevelUtil;
import com.wuyr.catchpiggy.utils.ThreadPool;

/**
 * Created by wuyr on 17-7-11 下午3:27.
 */

public class MainActivity extends BaseActivity {

    public static final int HOME = 0, LEVEL_SELECT = 1, CLASSIC = 2, PIGSTY = 3;
    private FrameLayout mRootView;
    private LoadingView mLoadingView;
    private HomeView mHomeView;
    private LevelSelectView mLevelSelectView;
    private ClassicModeView mClassicMode;
    private PigstyMode mPigstyMode;
    private AlertDialog mExitDialog;
    private MediaPlayer mPlayer;
    public int mCurrentStatus;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_main_view);
        initView();
    }

    private void initView() {
        mRootView = findViewById(R.id.root_view);
        mLoadingView = findViewById(R.id.loading_view);
        mHomeView = findViewById(R.id.home_view);
        mHomeView.setOnButtonClickListener(new HomeView.OnButtonClickListener() {
            @Override
            public void onPigstyModeButtonClicked() {
                showFixPigstyModeLevelSelectView();
            }

            @Override
            public void onClassicModeButtonClicked() {
                showClassicModeLevelSelectView();
            }

            @Override
            public boolean onSoundButtonClicked() {
                boolean isMusicStopped = false;
                if (mPlayer != null) {
                    if (mPlayer.isPlaying()) {
                        mPlayer.pause();
                        isMusicStopped = true;
                    } else {
                        mPlayer.start();
                    }
                }
                return isMusicStopped;
            }

            @Override
            public void onExitButtonClicked() {
                onBackPressed();
            }
        });
        mPlayer = MediaPlayer.create(this, R.raw.background_music);
        mPlayer.setOnPreparedListener(MediaPlayer::start);
        mPlayer.setLooping(true);
        mPlayer.setOnErrorListener((mp, what, extra) -> {
            mPlayer = null;
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCurrentStatus == HOME) {
            mHomeView.startShow();
        }
    }

    private void showFixPigstyModeLevelSelectView() {
        if (!mLoadingView.isLoading) {
            mLoadingView.startLoad(() -> {
                mCurrentStatus = LEVEL_SELECT;
                mHomeView.setVisibility(View.GONE);
                mHomeView.stopShow();
                mLevelSelectView = new LevelSelectView(this);
                mLevelSelectView.setMaxLevelCount(LevelUtil.PIGSTY_MODE_MAX_LEVEL + 1);
                mLevelSelectView.setValidHeartCount(Application.getPigstyModeCurrentValidHeartCount(this));
                mLevelSelectView.setValidLevelCount(Application.getCurrentPigstyModeLevel(this));
                mLevelSelectView.setOnLevelSelectedListener(this::startFixPigstyMode);
                mRootView.addView(mLevelSelectView, 0);
            });
        }
    }

    private void showClassicModeLevelSelectView() {
        if (!mLoadingView.isLoading) {
            mLoadingView.startLoad(() -> {
                mCurrentStatus = LEVEL_SELECT;
                mHomeView.setVisibility(View.GONE);
                mHomeView.stopShow();
                mLevelSelectView = new LevelSelectView(this);
                mLevelSelectView.setMaxLevelCount(LevelUtil.CLASSIC_MODE_MAX_LEVEL + 1);
                mLevelSelectView.setValidHeartCount(Application.getClassicModeCurrentValidHeartCount(this));
                mLevelSelectView.setValidLevelCount(Application.getCurrentClassicModeLevel(this));
                mLevelSelectView.setOnLevelSelectedListener(this::startClassicMode);
                mRootView.addView(mLevelSelectView, 0);
            });
        }
    }

    private void startFixPigstyMode(int level) {
        if (!mLoadingView.isLoading) {
            mLoadingView.startLoad(() -> {
                if (mLevelSelectView != null) {
                    mLevelSelectView.release();
                    mRootView.removeView(mLevelSelectView);
                    mLevelSelectView = null;
                }
                mCurrentStatus = PIGSTY;
                mPigstyMode = new PigstyMode(this);
                mPigstyMode.setCurrentLevel(level > LevelUtil.PIGSTY_MODE_MAX_LEVEL ? -1 : level);
                mRootView.addView(mPigstyMode, 0);
            });
        }
    }

    private void startClassicMode(int level) {
       if (!mLoadingView.isLoading) {
            mLoadingView.startLoad(() -> {
                if (mLevelSelectView != null) {
                    mLevelSelectView.release();
                    mRootView.removeView(mLevelSelectView);
                    mLevelSelectView = null;
                }
                mCurrentStatus = CLASSIC;
                mClassicMode = new ClassicModeView(this);
                mClassicMode.setCurrentLevel(level);
                mRootView.addView(mClassicMode, 0);
            });
        }
    }

    public void reloadFixPigstyMode(final int level) {
        mLoadingView.startLoad(() -> {
            mPigstyMode.exitNow();
            if (mPigstyMode != null) {
                mRootView.removeView(mPigstyMode);
            }
            mPigstyMode = new PigstyMode(MainActivity.this);
            mPigstyMode.setCurrentLevel(level > LevelUtil.PIGSTY_MODE_MAX_LEVEL ? -1 : level);
            mRootView.addView(mPigstyMode, 0);
        });
    }

    public void backToHome() {
        switch (mCurrentStatus) {
            case LEVEL_SELECT:
                if (!mLoadingView.isLoading) {
                    mLoadingView.startLoad(() -> {
                        if (mLevelSelectView != null) {
                            mLevelSelectView.release();
                            mRootView.removeView(mLevelSelectView);
                            mLevelSelectView = null;
                        }
                        resetHomeState();
                    });
                }
                break;
            case PIGSTY:
                mPigstyMode.exit(() -> mLoadingView.startLoad(() -> {
                    mRootView.removeView(mPigstyMode);
                    resetHomeState();
                }));
                break;
            case CLASSIC:
                mClassicMode.exit(() -> mLoadingView.startLoad(() -> {
                    mRootView.removeView(mClassicMode);
                    resetHomeState();
                }));
                break;
            default:
                break;
        }
    }

    private void resetHomeState() {
        mCurrentStatus = HOME;
        mHomeView.setVisibility(View.VISIBLE);
        mHomeView.startShow();
    }

    private void showExitDialog() {
        if (mExitDialog == null) {
            initExitDialog();
        }
        if (!mExitDialog.isShowing()) {
            mExitDialog.show();
        }
    }

    private void initExitDialog() {
        mExitDialog = new AlertDialog.Builder(this).setMessage(R.string.exit_dialog_message)
                .setNegativeButton(R.string.no, null).setPositiveButton(R.string.yes, (dialog, which) -> finish()).create();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (mCurrentStatus == PIGSTY && mPigstyMode.isStopped()) {
            mPigstyMode.restart();
        }
        if (mPlayer != null && !mPlayer.isPlaying() && !mHomeView.isMute) {
            mPlayer.start();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.pause();
        }
        if (mCurrentStatus == PIGSTY && !mPigstyMode.isStopped()) {
            mPigstyMode.stop();
        }
        if (mCurrentStatus == HOME) {
            mHomeView.stopShow();
        }
    }

    @Override
    public void onBackPressed() {
        switch (mCurrentStatus) {
            case HOME:
                showExitDialog();
                break;
            case LEVEL_SELECT:
            case PIGSTY:
            case CLASSIC:
                backToHome();
                break;
            default:
                break;
        }
    }

    @Override
    public void finish() {
        super.finish();
        ThreadPool.shutdown();
        if (mPlayer != null) {
            if (mPlayer.isPlaying()) {
                mPlayer.stop();
            }
            mPlayer.release();
            mPlayer = null;
        }
        mRootView = null;
        mExitDialog = null;
        mLoadingView = null;
        mPigstyMode = null;
        mClassicMode = null;
        mHomeView.release();
        mHomeView = null;
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}