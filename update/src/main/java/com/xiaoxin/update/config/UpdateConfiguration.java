package com.xiaoxin.update.config;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.xiaoxin.update.DefaultVersionProvider;
import com.xiaoxin.update.VersionInfoProvider;
import com.xiaoxin.update.listener.OnDownloadListener;
import com.xiaoxin.update.util.FileUtil;

import java.io.File;

/**
 * Created by liyuanbiao on 2016/9/17.
 */

public class UpdateConfiguration {
    private Context context;
    //检测升级url
    private String updateUrl;
    //apk下载存放地址
    private String targetFile;
    private String patchTargetFile;
    //是否开启debug
    private boolean debug;
    //是否静默安装
    private boolean silence;
    //是否再下载时显示通知栏，设置非静默安装时生效
    private boolean showUI;
    //是否选择增量更新，versioninfo的patchUrl有值时可用
    private boolean increment;
    //选择安装模式，设置静默安装，且使用系统签名时生效
    private InstallMode installMode = InstallMode.PM;
    //设置为true则表示，用户正在操作时采用普通升级
    private boolean isFriendly;
    //下载时显示的图标的资源id
    private int icon;
    //版本信息提供者
    private VersionInfoProvider versionInfoProvider;
    //下载监听
    private OnDownloadListener downloadListener;
    //间隔多长时间检测一次,<1000*60*30 小于半小时不检测
    private long checkSpan;
    private String downloadUrl;


    public UpdateConfiguration(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public String getUpdateUrl() {
        return updateUrl;
    }

    public void setUpdateUrl(String updateUrl) {
        this.updateUrl = updateUrl;
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

    public boolean isSilence() {
        return silence;
    }

    public void setSilence(boolean silence) {
        this.silence = silence;
    }

    public boolean isShowUI() {
        return showUI;
    }

    public void setShowUI(boolean showUI) {
        this.showUI = showUI;
    }

    public boolean isIncrement() {
        return increment;
    }

    public void setIncrement(boolean increment) {
        this.increment = increment;
    }

    public InstallMode getInstallMode() {
        return installMode;
    }

    public void setInstallMode(InstallMode installMode) {
        this.installMode = installMode;
    }

    public boolean isFriendly() {
        return isFriendly;
    }

    public void setFriendly(boolean friendly) {
        isFriendly = friendly;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public VersionInfoProvider getVersionInfoProvider() {
        return versionInfoProvider;
    }

    public void setVersionInfoProvider(VersionInfoProvider versionInfoProvider) {
        this.versionInfoProvider = versionInfoProvider;
    }

    public OnDownloadListener getDownloadListener() {
        return downloadListener;
    }

    public void setDownloadListener(OnDownloadListener downloadListener) {
        this.downloadListener = downloadListener;
    }

    public long getCheckSpan() {
        return checkSpan;
    }

    public void setCheckSpan(long checkSpan) {
        this.checkSpan = checkSpan;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getPatchTargetFile() {
        return patchTargetFile;
    }

    public void setPatchTargetFile(String patchTargetFile) {
        this.patchTargetFile = patchTargetFile;
    }

    public static class Builder {
        UpdateConfiguration configuration;

        public Builder(Context context) {
            configuration = new UpdateConfiguration(context.getApplicationContext());
        }

        public Context getContext() {
            return configuration.getContext();
        }

        public Builder setContext(Context context) {
            configuration.setContext(context);
            return this;
        }

        public String getUpdateUrl() {
            return configuration.getUpdateUrl();
        }

        public Builder setUpdateUrl(String updateUrl) {
            configuration.setUpdateUrl(updateUrl);
            return this;
        }

        public String getTargetFile() {
            return configuration.getTargetFile();
        }

        public Builder setTargetFile(String targetFile) {
            configuration.setTargetFile(targetFile);
            return this;
        }

        public boolean isDebug() {
            return configuration.isDebug();
        }

        public Builder setDebug(boolean debug) {
            configuration.setDebug(debug);
            return this;
        }

        public boolean isSilence() {
            return configuration.isSilence();
        }

        public Builder setSilence(boolean silence) {
            configuration.setSilence(silence);
            return this;
        }

        public boolean isShowUI() {
            return configuration.isShowUI();
        }

        public Builder setShowUI(boolean showUI) {
            configuration.setShowUI(showUI);
            return this;
        }

        public boolean isIncrement() {
            return configuration.isIncrement();
        }

        public Builder setIncrement(boolean increment) {
            configuration.setIncrement(increment);
            return this;
        }

        public InstallMode getInstallMode() {
            return configuration.getInstallMode();
        }

        public Builder setInstallMode(InstallMode installMode) {
            configuration.setInstallMode(installMode);
            return this;
        }

        public boolean isFriendly() {
            return configuration.isFriendly();
        }

        public Builder setFriendly(boolean friendly) {
            configuration.setFriendly(friendly);
            return this;
        }

        public int getIcon() {
            return configuration.getIcon();
        }

        public Builder setIcon(int icon) {
            configuration.setIcon(icon);
            return this;
        }

        public VersionInfoProvider getVersionInfoProvider() {
            return configuration.getVersionInfoProvider();
        }

        public Builder setVersionInfoProvider(VersionInfoProvider versionInfoProvider) {
            configuration.setVersionInfoProvider(versionInfoProvider);
            return this;
        }

        public OnDownloadListener getDownloadListener() {
            return configuration.getDownloadListener();
        }

        public Builder setDownloadListener(OnDownloadListener downloadListener) {
            configuration.setDownloadListener(downloadListener);
            return this;
        }

        public long getCheckSpan() {
            return configuration.getCheckSpan();
        }

        public Builder setCheckSpan(long checkSpan) {
            configuration.setCheckSpan(checkSpan);
            return this;
        }

        public String getDownloadUrl() {
            return configuration.getDownloadUrl();
        }

        public Builder setDownloadUrl(String downloadUrl) {
            configuration.setDownloadUrl(downloadUrl);
            return this;
        }

        public String getPatchTargetFile() {
            return configuration.getPatchTargetFile();
        }

        public void setPatchTargetFile(String patchTargetFile) {
            configuration.setPatchTargetFile(patchTargetFile);
        }

        public UpdateConfiguration build() {
            if (getIcon() == 0) {
                setIcon(android.R.drawable.sym_def_app_icon);
            }

            if (TextUtils.isEmpty(getTargetFile())) {
                String defaultPath = FileUtil.getFile(Environment.getExternalStorageDirectory(), "download.apk").getAbsolutePath();
                configuration.setTargetFile(defaultPath);
            }

            if (getDownloadListener() == null) {
                setDownloadListener(OnDownloadListener.EMPTY);
            }
            if (getVersionInfoProvider() == null) {
                configuration.setVersionInfoProvider(new DefaultVersionProvider(getContext()));
            }

            if (TextUtils.isEmpty(getPatchTargetFile())) {
                setPatchTargetFile(new File(Environment.getExternalStorageDirectory()
                        , "p.patch").getAbsolutePath());
            }
            return configuration;
        }
    }

}
