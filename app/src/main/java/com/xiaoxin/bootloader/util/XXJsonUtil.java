package com.xiaoxin.bootloader.util;

import com.xiaoxin.update.XXVersionInfoProvider;
import com.xiaoxin.update.bean.XXVersionInfo;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by liyuanbiao on 2016/9/18.
 */

public class XXJsonUtil {
    /**
     * private String packageName;
     * private String versionCode;
     * private String updateInfo;
     * private String updateUrl;
     */
    public static XXVersionInfoProvider versionInfoProvider = new XXVersionInfoProvider() {
        @Override
        public XXVersionInfo provider(String result) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                String packageName = jsonObject.getString("packageName");
                int versionCode = jsonObject.getInt("versionCode");
                String updateInfo = jsonObject.getString("updateInfo");
                String updateUrl = jsonObject.getString("updateUrl");

                XXVersionInfo versionInfo = new XXVersionInfo();
                versionInfo.setPackageName(packageName);
                versionInfo.setVersionCode(versionCode);
                versionInfo.setUpdateInfo(updateInfo);
                versionInfo.setDownloadUrl(updateUrl);
                return versionInfo;
            } catch (JSONException e) {
                return null;
            }
        }
    };
}
