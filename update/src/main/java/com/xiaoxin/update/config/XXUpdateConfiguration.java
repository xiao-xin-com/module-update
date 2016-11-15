package com.xiaoxin.update.config;

import android.os.Environment;
import android.text.TextUtils;

import com.xiaoxin.update.XXDefaultVersionProvider;
import com.xiaoxin.update.XXVersionInfoProvider;
import com.xiaoxin.update.listener.XXDownloadListener;
import com.xiaoxin.update.util.XXFileUtil;

import java.io.File;

/**
 * Created by liyuanbiao on 2016/9/17.
 */

public class XXUpdateConfiguration {
    //检测升级url
    private String updateUrl;
    //apk下载地址
    private String downloadUrl;
    //apk下载存放地址
    private String targetFile;
    //是否开启debug
    private boolean debug;
    //是否静默安装
    private boolean silence;
    //是否再下载时显示通知栏，设置非静默安装时生效
    private boolean showUI;
    //是否使用pm安装，设置静默安装，且使用系统签名时生效
    private boolean usePm;
    //下载时显示的图标的资源id
    private int icon;
    //版本信息提供者
    private XXVersionInfoProvider versionInfoProvider;
    //下载监听
    private XXDownloadListener downloadListener;

    protected XXUpdateConfiguration(){

    }

    public boolean isUsePm() {
        return usePm;
    }

    public XXUpdateConfiguration setUsePm(boolean usePm) {
        this.usePm = usePm;
        return this;
    }

    public XXDownloadListener getDownloadListener() {
        return downloadListener;
    }

    public XXUpdateConfiguration setDownloadListener(XXDownloadListener downloadListener) {
        this.downloadListener = downloadListener;
        return this;
    }

    public String getUpdateUrl() {
        return updateUrl;
    }

    public XXUpdateConfiguration setUpdateUrl(String updateUrl) {
        this.updateUrl = updateUrl;
        return this;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public XXUpdateConfiguration setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
        return this;
    }

    public String getTargetFile() {
        return targetFile;
    }

    public XXUpdateConfiguration setTargetFile(String targetFile) {
        this.targetFile = targetFile;
        return this;
    }

    public boolean isDebug() {
        return debug;
    }

    public XXUpdateConfiguration setDebug(boolean debug) {
        this.debug = debug;
        return this;
    }

    public boolean isSilence() {
        return silence;
    }

    public XXUpdateConfiguration setSilence(boolean silence) {
        this.silence = silence;
        return this;
    }

    public boolean isShowUI() {
        return showUI;
    }

    public XXUpdateConfiguration setShowUI(boolean showUI) {
        this.showUI = showUI;
        return this;
    }

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

    public int getIcon() {
        return icon;
    }

    public XXUpdateConfiguration setIcon(int icon) {
        this.icon = icon;
        return this;
    }

    public static class Builder {
        private String updateUrl;
        private String downloadUrl;
        private String targetFile;
        private boolean debug;
        private boolean silence;
        private boolean showUI;
        private boolean usePm;
        private int icon;
        private XXVersionInfoProvider versionInfoProvider;
        private XXDownloadListener downloadListener;

        {
            debug = false;
            silence = true;
            usePm = true;
            versionInfoProvider = new XXDefaultVersionProvider();
            downloadListener = XXDownloadListener.EMPTY;
            File externalStorageDirectory = Environment.getExternalStorageDirectory();
            if (externalStorageDirectory != null && externalStorageDirectory.exists() && externalStorageDirectory.isDirectory()) {
                targetFile = new File(externalStorageDirectory, "download.apk").getAbsolutePath();
            }
        }

        public Builder setUsePm(boolean usePm) {
            this.usePm = usePm;
            return this;
        }

        public Builder setVersionInfoProvider(XXVersionInfoProvider versionInfoProvider) {
            this.versionInfoProvider = versionInfoProvider;
            return this;
        }

        public Builder setUpdateUrl(String updateUrl) {
            this.updateUrl = updateUrl;
            return this;
        }

        public Builder setDownloadUrl(String downloadUrl) {
            this.downloadUrl = downloadUrl;
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
            this.silence = silence;
            return this;
        }

        public Builder setShowUI(boolean showUI) {
            this.showUI = showUI;
            return this;
        }

        public Builder setIcon(int icon) {
            this.icon = icon;
            return this;
        }

        public XXUpdateConfiguration build() {
            XXUpdateConfiguration configuration = new XXUpdateConfiguration();
            configuration.setDebug(debug);
            configuration.setSilence(silence);
            configuration.setShowUI(showUI);
            configuration.setUsePm(usePm);
            configuration.setIcon(icon == 0 ? android.R.drawable.sym_def_app_icon : icon);
            if (!TextUtils.isEmpty(downloadUrl)) {
                configuration.setDownloadUrl(downloadUrl);
            }
            if (!TextUtils.isEmpty(updateUrl)) {
                configuration.setUpdateUrl(updateUrl);
            }
            configuration.setTargetFile(TextUtils.isEmpty(targetFile) ? XXFileUtil.getFile(Environment.getExternalStorageDirectory(), "download.apk").getAbsolutePath() : targetFile);
            configuration.setDownloadListener(downloadListener == null ? XXDownloadListener.EMPTY : downloadListener);
            configuration.setVersionInfoProvider(versionInfoProvider);
            return configuration;
        }
    }

}
