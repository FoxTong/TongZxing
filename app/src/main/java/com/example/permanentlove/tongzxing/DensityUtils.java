package com.example.permanentlove.tongzxing;

import android.content.Context;

/**
 * Created by fengxitong on 2016/10/27.
 * Email address is m18824124885@163.com
 * dx与dp互相转换
 */

public class DensityUtils {
    public static int dp2px(Context context,float dp){
        float density=context.getResources().getDisplayMetrics().density;
        int px=(int)(dp*density+0.5f);
        return px;
    }
    public static float px2dp(Context context,int px){
        float density=context.getResources().getDisplayMetrics().density;
        float dp=px/density;
        return dp;
    }
}
