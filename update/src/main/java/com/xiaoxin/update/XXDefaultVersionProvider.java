package com.xiaoxin.update;

import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.xiaoxin.update.bean.XXRspUpdate;
import com.xiaoxin.update.bean.XXVersionInfo;
import com.xiaoxin.update.util.XXNumberUtil;

import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by liyuanbiao on 2016/9/25.
 */

public class XXDefaultVersionProvider implements XXVersionInfoProvider {

    public static String getUpdateUrl(Context context) {
        try {
            return new StringBuilder("http://120.76.232.3:1337/appVersion/verify?")
                    .append("packageName=").append(context.getPackageName()).append("&")
                    .append("platform=Android&versionCode=")
                    .append(context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode)
                    .toString();
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    @Override
    public XXVersionInfo provider(String s) {
        List<XXRspUpdate> updateList = null;
        try {
            updateList = new Gson().fromJson(s, new TypeToken<List<XXRspUpdate>>() {
            }.getType());
        } catch (JsonSyntaxException e) {
            Log.e(TAG, "provider: ", e);
        }
        if (updateList != null && !updateList.isEmpty() && updateList.get(0) != null) {
            XXRspUpdate rspUpdate = updateList.get(0);
            XXVersionInfo versionInfo = new XXVersionInfo();
            versionInfo.setVersionCode(XXNumberUtil.parseInt(rspUpdate.getVersionCode(), 0));
            versionInfo.setPackageName(rspUpdate.getPackageName());
            versionInfo.setUpdateInfo(getUpdateInfo(rspUpdate.getUpdateInfo()));
            versionInfo.setDownloadUrl(rspUpdate.getUpdateUrl());
            return versionInfo;
        }
        return null;
    }

    private String getUpdateInfo(List<String> updateInfo) {
        int number = 0;
        StringBuilder sb = null;
        if (updateInfo != null && !updateInfo.isEmpty()) {
            for (String item : updateInfo) {
                if (!TextUtils.isEmpty(item)) {
                    if (sb == null) {
                        sb = new StringBuilder();
                    }
                    sb.append(++number).append(".").append(item).append("\r\n");
                }
            }
        }
        return (sb == null || sb.length() <= 0) ? null : sb.toString();
    }
}
