package com.xiaoxin.update.helper;

import android.content.Context;

import com.xiaoxin.update.UpdateManager;
import com.xiaoxin.update.bean.AppInfo;
import com.xiaoxin.update.bean.PatchInfo;
import com.xiaoxin.update.listener.OnPatchListener;
import com.xiaoxin.update.util.NotifyUtil;

/**
 * Created by liyuanbiao on 2017/9/8.
 */

public class DispatchPatchEvent implements OnPatchListener {
    private Context context;
    private AppInfo appInfo;
    private int applicationIcon;
    private String applicationLabel;
    private int notifyId;

    public AppInfo getAppInfo() {
        return appInfo;
    }

    public DispatchPatchEvent(Context context, int notifyId) {
        this(context);
        this.notifyId = notifyId;
    }

    public DispatchPatchEvent(Context context) {
        this.context = context;
        this.appInfo = new AppInfo(context);
        this.applicationIcon = appInfo.getApplicationIcon();
        this.applicationLabel = appInfo.getApplicationLabel();
    }

    @Override
    public void onPrepare(PatchInfo patchInfo) {
        if (UpdateManager.isShowUI() && !UpdateManager.isSilence()) {
            NotifyUtil.create(context, notifyId).notify_progress(null,
                    applicationIcon, "组合增量包", applicationLabel, "开始组合增量包",
                    false, false, false, 100, 100, false);
        }
    }

    @Override
    public void onComplete(PatchInfo patchInfo) {
        if (UpdateManager.isShowUI() && !UpdateManager.isSilence()) {
            NotifyUtil.create(context, notifyId).notify_progress(null,
                    applicationIcon, "组合增量包", applicationLabel, "组合完成",
                    false, false, false, 100, 100, false);
        }
    }

    @Override
    public void onError(Exception e) {
        if (UpdateManager.isShowUI() && !UpdateManager.isSilence()) {
            NotifyUtil.create(context, notifyId).notify_progress(null,
                    applicationIcon, "组合增量包", applicationLabel, "组合失败，将为您下载整包升级",
                    false, false, false, 100, 100, false);
        }
    }
}
