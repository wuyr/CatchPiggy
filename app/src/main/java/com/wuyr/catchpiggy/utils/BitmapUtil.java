package com.wuyr.catchpiggy.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.DrawableRes;

/**
 * Created by wuyr on 17-11-11 下午3:55.
 */

public class BitmapUtil {

    public static Bitmap scaleBitmap(Bitmap target, int w, int h) {
        int width = target.getWidth();
        int height = target.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(((float) w / width), ((float) h / height));
        return Bitmap.createBitmap(target, 0, 0, width, height, matrix, true);
    }

    public static BitmapDrawable scaleDrawable(BitmapDrawable drawable, int w, int h) {
        Bitmap oldBitmap = drawable.getBitmap();
        int width = oldBitmap.getWidth();
        int height = oldBitmap.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidth = ((float) w / width);
        float scaleHeight = ((float) h / height);
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newBitmap = Bitmap.createBitmap(oldBitmap, 0, 0, width, height, matrix, true);
        return new BitmapDrawable(null, newBitmap);
    }

    public static Bitmap getBitmapFromResource(Context context, @DrawableRes int id) {
        return BitmapFactory.decodeResource(context.getResources(), id);
    }

    public static Bitmap toGray(Bitmap target) {
        Bitmap temp = target.copy(Bitmap.Config.ARGB_8888, true);
        int width = temp.getWidth(), height = temp.getHeight();
        int[] targetPixels = new int[width * height];
        //获取bitmap所有像素点
        temp.getPixels(targetPixels, 0, width, 0, 0, width, height);
        int index = 0;
        int pixelColor;
        int a, r, g, b;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                //获取rgb色值并与目标颜色相比较
                pixelColor = targetPixels[index];
                a = Color.alpha(pixelColor);
                r = Color.red(pixelColor);
                g = Color.green(pixelColor);
                b = Color.blue(pixelColor);
                int gray = (r + g + b) / 3;
                targetPixels[index] = Color.argb(a, gray, gray, gray);
                ++index;
            }
        }
        temp.setPixels(targetPixels, 0, width, 0, 0, width, height);
        return temp;
    }
}
