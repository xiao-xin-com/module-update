package com.xiaoxin.update.config;

import android.os.Environment;
import android.text.TextUtils;

import com.xiaoxin.update.XXDefaultVersionProvider;
import com.xiaoxin.update.XXVersionInfoProvider;
import com.xiaoxin.update.listener.XXDownloadListener;
import com.xiaoxin.update.util.XXCmdUtil;
import com.xiaoxin.update.util.XXFileUtil;

/**
 * Created by liyuanbiao on 2016/9/17.
 */

public class XXUpdateConfiguration {
    private String updateUrl;
    private String apkDownloadUrl;
    private String targetFile;
    private boolean debug;
    private boolean silence;
    private XXVersionInfoProvider versionInfoProvider;
    private XXDownloadListener downloadListener;


    public XXVersionInfoProvider getVersionInfoProvider() {
        return versionInfoProvider;
    }

    public XXUpdateConfiguration setVersionInfoProvider(XXVersionInfoProvider versionInfoProvider) {
        this.versionInfoProvider = versionInfoProvider;
        if (this.versionInfoProvider == null) {
            this.versionInfoProvider = new XXDefaultVersionProvider();
        }
        return this;
    }

    public boolean isSilence() {
        return silence;
    }

    public XXUpdateConfiguration setSilence(boolean silence) {
        this.silence = silence;
        return this;
    }

    private XXUpdateConfiguration() {
    }

    public String getUpdateUrl() {
        return updateUrl;
    }

    public void setUpdateUrl(String updateUrl) {
        this.updateUrl = updateUrl;
    }

    public String getApkDownloadUrl() {
        return apkDownloadUrl;
    }

    public void setApkDownloadUrl(String apkDownloadUrl) {
        this.apkDownloadUrl = apkDownloadUrl;
    }

    public String getTargetFile() {
        return targetFile;
    }

    public void setTargetFile(String targetFile) {
        this.targetFile = targetFile;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public XXDownloadListener getDownloadListener() {
        return downloadListener;
    }

    public void setDownloadListener(XXDownloadListener downloadListener) {
        this.downloadListener = downloadListener;
    }

    public static class Builder {
        private String updateUrl;
        private String apkDownloadUrl;
        private String targetFile;
        private boolean debug;
        private boolean silence;
        private XXVersionInfoProvider versionInfoProvider;
        private XXDownloadListener downloadListener;

        public Builder setVersionInfoProvider(XXVersionInfoProvider versionInfoProvider) {
            this.versionInfoProvider = versionInfoProvider;
            return this;
        }

        public Builder setUpdateUrl(String updateUrl) {
            this.updateUrl = updateUrl;
            return this;
        }

        public Builder setApkDownloadUrl(String apkDownloadUrl) {
            this.apkDownloadUrl = apkDownloadUrl;
            return this;
        }

        public Builder setTargetFile(String targetFile) {
            this.targetFile = targetFile;
            return this;
        }

        public Builder setDebug(boolean debug) {
            this.debug = debug;
            return this;
        }

        public Builder setDownloadListener(XXDownloadListener downloadListener) {
            this.downloadListener = downloadListener;
            return this;
        }

        public Builder setSilence(boolean silence) {
            if (XXCmdUtil.isRoot()) {
                this.silence = silence;
            }
            return this;
        }

        public XXUpdateConfiguration build() {
            XXUpdateConfiguration configuration = new XXUpdateConfiguration();
            configuration.setDebug(debug);
            configuration.setSilence(silence);
            if (!TextUtils.isEmpty(apkDownloadUrl))
                configuration.setApkDownloadUrl(apkDownloadUrl);
            if (!TextUtils.isEmpty(updateUrl))
                configuration.setUpdateUrl(updateUrl);
            configuration.setTargetFile(TextUtils.isEmpty(targetFile) ? XXFileUtil.getFile(Environment.getExternalStorageDirectory(), "download.apk").getAbsolutePath() : targetFile);
            configuration.setDownloadListener(downloadListener == null ? XXDownloadListener.EMPTY : downloadListener);
            configuration.setVersionInfoProvider(versionInfoProvider);
            return configuration;
        }
    }

}
