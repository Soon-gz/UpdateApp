package com.abings.updateapp;

import android.content.Context;
import android.provider.Settings;

public class AppUtils {

    public AppUtils() {
    }

    public static int getVersionCode(Context context) {
        int i;
        try {
            i = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
        return i;
    }

    public static String getVersionName(Context context) {
        String s;
        try {
            s = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
        return s;
    }

    public static String getDeviceId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static String getSystemVersion() {
        return android.os.Build.VERSION.RELEASE;// android系统版本号
    }

}
