package cn.com.tianyudg.pulltorefreshdemo.util;

import android.content.Context;

/**
 * @author Administrator;
 * @date 201704201534
 * @desc
 * @email
 */

public class DensityUtil {
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
