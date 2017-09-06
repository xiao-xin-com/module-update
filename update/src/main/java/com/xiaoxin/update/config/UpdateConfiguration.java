package com.xiaoxin.update.config;

import android.os.Environment;
import android.text.TextUtils;

import com.xiaoxin.update.DefaultVersionProvider;
import com.xiaoxin.update.VersionInfoProvider;
import com.xiaoxin.update.listener.OnDownloadListener;
import com.xiaoxin.update.util.FileUtil;
import com.xiaoxin.update.util.UpdateLog;

import java.io.File;

/**
 * Created by liyuanbiao on 2016/9/17.
 */

public class UpdateConfiguration {
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
    //设置为true则表示，用户正在操作时采用普通升级
    private boolean isFriendly;
    //下载时显示的图标的资源id
    private int icon;
    //版本信息提供者
    private VersionInfoProvider versionInfoProvider;
    //下载监听
    private OnDownloadListener downloadListener;
    //间隔多长时间检测一次
    private long checkSpan;

    protected UpdateConfiguration() {

    }

    public boolean isFriendly() {
        return isFriendly;
    }

    public UpdateConfiguration setFriendly(boolean friendly) {
        isFriendly = friendly;
        return this;
    }

    public boolean isUsePm() {
        return usePm;
    }

    public UpdateConfiguration setUsePm(boolean usePm) {
        this.usePm = usePm;
        return this;
    }

    public long getCheckSpan() {
        return checkSpan;
    }

    public void setCheckSpan(long checkSpan) {
        this.checkSpan = checkSpan;
    }

    public OnDownloadListener getDownloadListener() {
        return downloadListener;
    }

    public UpdateConfiguration setDownloadListener(OnDownloadListener downloadListener) {
        this.downloadListener = downloadListener;
        return this;
    }

    public String getUpdateUrl() {
        return updateUrl;
    }

    public UpdateConfiguration setUpdateUrl(String updateUrl) {
        this.updateUrl = updateUrl;
        return this;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public UpdateConfiguration setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
        return this;
    }

    public String getTargetFile() {
        return targetFile;
    }

    public UpdateConfiguration setTargetFile(String targetFile) {
        this.targetFile = targetFile;
        return this;
    }

    public boolean isDebug() {
        return debug;
    }

    public UpdateConfiguration setDebug(boolean debug) {
        this.debug = debug;
        return this;
    }

    public boolean isSilence() {
        return silence;
    }

    public UpdateConfiguration setSilence(boolean silence) {
        this.silence = silence;
        return this;
    }

    public boolean isShowUI() {
        return showUI;
    }

    public UpdateConfiguration setShowUI(boolean showUI) {
        this.showUI = showUI;
        return this;
    }

    public VersionInfoProvider getVersionInfoProvider() {
        return versionInfoProvider;
    }

    public UpdateConfiguration setVersionInfoProvider(VersionInfoProvider versionInfoProvider) {
        this.versionInfoProvider = versionInfoProvider;
        if (this.versionInfoProvider == null) {
            this.versionInfoProvider = new DefaultVersionProvider();
        }
        return this;
    }

    public int getIcon() {
        return icon;
    }

    public UpdateConfiguration setIcon(int icon) {
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
        private boolean isFriendly;
        private int icon;
        private long checkSpan;
        private VersionInfoProvider versionInfoProvider;
        private OnDownloadListener downloadListener;

        {
            debug = false;
            silence = true;
            usePm = true;
            isFriendly = false;
            versionInfoProvider = new DefaultVersionProvider();
            downloadListener = OnDownloadListener.EMPTY;
            File externalStorageDirectory = Environment.getExternalStorageDirectory();
            if (externalStorageDirectory != null && externalStorageDirectory.exists() && externalStorageDirectory.isDirectory()) {
                targetFile = new File(externalStorageDirectory, "download.apk").getAbsolutePath();
            }
        }

        public Builder setFriendly(boolean friendly) {
            isFriendly = friendly;
            return this;
        }

        public Builder setUsePm(boolean usePm) {
            this.usePm = usePm;
            return this;
        }

        public Builder setCheckSpan(long checkSpan) {
            this.checkSpan = checkSpan;
            return this;
        }

        public Builder setVersionInfoProvider(VersionInfoProvider versionInfoProvider) {
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
            UpdateLog.setLogFlag(this.debug);
            return this;
        }

        public Builder setDownloadListener(OnDownloadListener downloadListener) {
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

        public UpdateConfiguration build() {
            UpdateConfiguration configuration = new UpdateConfiguration();
            configuration.setDebug(debug);
            configuration.setSilence(silence);
            configuration.setShowUI(showUI);
            configuration.setUsePm(usePm);
            configuration.setFriendly(isFriendly);
            configuration.setCheckSpan(checkSpan);
            configuration.setIcon(icon == 0 ? android.R.drawable.sym_def_app_icon : icon);
            if (!TextUtils.isEmpty(downloadUrl)) {
                configuration.setDownloadUrl(downloadUrl);
            }
            if (!TextUtils.isEmpty(updateUrl)) {
                configuration.setUpdateUrl(updateUrl);
            }
            configuration.setTargetFile(TextUtils.isEmpty(targetFile) ? FileUtil.getFile(Environment.getExternalStorageDirectory(), "download.apk").getAbsolutePath() : targetFile);
            configuration.setDownloadListener(downloadListener == null ? OnDownloadListener.EMPTY : downloadListener);
            configuration.setVersionInfoProvider(versionInfoProvider);
            return configuration;
        }
    }

}
