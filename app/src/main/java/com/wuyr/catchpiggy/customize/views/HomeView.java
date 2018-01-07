package com.wuyr.catchpiggy.customize.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import com.wuyr.catchpiggy.R;

/**
 * Created by wuyr on 17-12-24 下午11:24.
 */

public class HomeView extends RelativeLayout {

    private OnButtonClickListener mOnButtonClickListener;
    public boolean isMute;

    public HomeView(Context context) {
        this(context, null);
    }

    public HomeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HomeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_home, this, true);
        OnClickListener onClickListener = v -> {
            if (mOnButtonClickListener != null) {
                switch (v.getId()) {
                    case R.id.fix_pigsty_mode_btn:
                        mOnButtonClickListener.onPigstyModeButtonClicked();
                        break;
                    case R.id.classic_mode_btn:
                        mOnButtonClickListener.onClassicModeButtonClicked();
                        break;
                    case R.id.exit_btn:
                        mOnButtonClickListener.onExitButtonClicked();
                        break;
                    case R.id.sound_btn:
                        v.setBackgroundResource((isMute = mOnButtonClickListener.onSoundButtonClicked()) ? R.mipmap.ic_mute : R.mipmap.ic_sound);
                        break;
                    default:
                        break;
                }
            }
        };
        findViewById(R.id.fix_pigsty_mode_btn).setOnClickListener(onClickListener);
        findViewById(R.id.classic_mode_btn).setOnClickListener(onClickListener);
        findViewById(R.id.exit_btn).setOnClickListener(onClickListener);
        findViewById(R.id.sound_btn).setOnClickListener(onClickListener);
    }

    public void startShow() {
        //使用post是为了确保已经初始化完成
        post(() -> ((RandomPiggies) findViewById(R.id.random_piggies)).startShow());
    }

    public void stopShow() {
        ((RandomPiggies) findViewById(R.id.random_piggies)).stopShow();
    }

    public void setOnButtonClickListener(OnButtonClickListener onButtonClickListener) {
        mOnButtonClickListener = onButtonClickListener;
    }

    public void release() {
        stopShow();
        if (mOnButtonClickListener != null) {
            mOnButtonClickListener = null;
        }
    }

    public interface OnButtonClickListener {
        void onPigstyModeButtonClicked();

        void onClassicModeButtonClicked();

        boolean onSoundButtonClicked();

        void onExitButtonClicked();
    }
}
