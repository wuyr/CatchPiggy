package com.wuyr.catchpiggy.utils;

import android.content.Context;

import com.wuyr.catchpiggy.R;

import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.tencent.qzone.QZone;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;

/**
 * Created by wuyr on 17-12-29 下午8:43.
 */

public class ShareUtil {

    public static void shareToWeChat(Context context, boolean isRequestHelp, String message) {
        OnekeyShare onekeyShare = getData(context, isRequestHelp, message);
        onekeyShare.setPlatform(Wechat.NAME);
        onekeyShare.show(context);
    }

    public static void shareToWeChatMoments(Context context, boolean isRequestHelp, String message) {
        OnekeyShare onekeyShare = getData(context, isRequestHelp, message);
        onekeyShare.setPlatform(WechatMoments.NAME);
        onekeyShare.show(context);
    }

    public static void shareToQQ(Context context, boolean isRequestHelp, String message) {
        OnekeyShare onekeyShare = getData(context, isRequestHelp, message);
        onekeyShare.setPlatform(QQ.NAME);
        onekeyShare.show(context);
    }

    public static void shareToQZone(Context context, boolean isRequestHelp, String message) {
        OnekeyShare onekeyShare = getData(context, isRequestHelp, message);
        onekeyShare.setPlatform(QZone.NAME);
        onekeyShare.show(context);
    }

    private static OnekeyShare getData(Context context, boolean isRequestHelp, String message) {
        String url = "https://wuyr.github.io/";
        OnekeyShare onekeyShare = new OnekeyShare();
        onekeyShare.setImageUrl("https://wuyr.github.io/files/icon.png");
        onekeyShare.setTitle("来自捉小猪的分享");
        onekeyShare.setText(isRequestHelp ? context.getString(R.string.request_help_format) : message);
        onekeyShare.setSite(context.getResources().getString(R.string.app_name));
        onekeyShare.setTitleUrl(url);
        onekeyShare.setSiteUrl(url);
        onekeyShare.setUrl(url);
        return onekeyShare;
    }
}
