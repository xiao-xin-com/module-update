package com.xiaoxin.update;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.xiaoxin.update.bean.DefaultResponse;
import com.xiaoxin.update.bean.PatchUrl;
import com.xiaoxin.update.bean.VersionInfo;
import com.xiaoxin.update.util.GetAppInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;

/**
 * Created by liyuanbiao on 2016/9/25.
 */

public class DefaultVersionProvider implements VersionInfoProvider {

    public static String getUpdateUrl(Context context) {
        return getUpdateUrl(context, null);
    }

    public static String getUpdateUrl(Context context, String model) {
        return new StringBuilder("https://xxtserver.xiao-xin.com/appVersion/verify?")
                .append("packageName=").append(GetAppInfo.getAppPackageName(context))
                .append("&platform=Android&versionCode=")
                .append(GetAppInfo.getAppVersionCode(context))
                .append("&model=").append(model == null ? "" : model)
                .toString();
    }

    private Context context;

    public DefaultVersionProvider(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public VersionInfo provider(String s) {
        List<DefaultResponse> updateList = null;
        try {
            updateList = new Gson().fromJson(s, new TypeToken<List<DefaultResponse>>() {
            }.getType());
        } catch (JsonSyntaxException e) {
            Log.e(TAG, "provider: ", e);
        }
        if (updateList != null && !updateList.isEmpty() && updateList.get(0) != null) {
            DefaultResponse rspUpdate = updateList.get(0);
            VersionInfo versionInfo = new VersionInfo();
            versionInfo.setPackageName(rspUpdate.getPackageName());
            versionInfo.setVersionCode(rspUpdate.getVersionCode());
            versionInfo.setUpdateUrl(rspUpdate.getUpdateUrl());
            versionInfo.setMd5checksum(rspUpdate.getMd5checksum());
            versionInfo.setModel(rspUpdate.getModel());

            Map<String, PatchUrl> patchUrl = rspUpdate.getPatchUrl();
            if (patchUrl != null) {
                int versionCode = GetAppInfo.getAppVersionCode(context);
                versionInfo.setPatchUrl(patchUrl.get(String.valueOf(versionCode)));
            }

            versionInfo.setInstallFilename(rspUpdate.getInstallFilename());
            versionInfo.setUpdateInfo(filterEmpty(rspUpdate.getUpdateInfo()));
            versionInfo.setDetail(getUpdateInfo(versionInfo.getUpdateInfo()));
            return versionInfo;
        }
        return null;
    }

    private static List<String> filterEmpty(List<String> list) {
        if (list == null) return new ArrayList<>();
        List<String> stringList = new ArrayList<>();
        for (String item : list) {
            if (!TextUtils.isEmpty(item) && !TextUtils.equals(item, "desc")) {
                stringList.add(item);
            }
        }
        return stringList;
    }

    public static String getUpdateInfo(List<String> updateInfo) {
        return getUpdateInfo(updateInfo, true);
    }

    public static String getUpdateInfo(List<String> updateInfo, boolean isOrderedList) {
        int number = 0;
        StringBuilder sb = null;
        if (updateInfo != null && !updateInfo.isEmpty()) {
            for (String item : updateInfo) {
                if (!TextUtils.isEmpty(item)) {
                    if (sb == null) {
                        sb = new StringBuilder();
                    }
                    ++number;
                    if (isOrderedList) {
                        sb.append(number).append(".");
                    } else {
                        sb.append("•");
                    }
                    sb.append(" ").append(item).append("\r\n");
                }
            }
        }
        return (sb == null || sb.length() <= 0) ? null : sb.toString();
    }
}
