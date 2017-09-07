package com.xiaoxin.update.task.patch;

import com.cundong.utils.PatchUtils;
import com.xiaoxin.update.bean.PatchInfo;
import com.xiaoxin.update.bean.PatchUrl;
import com.xiaoxin.update.helper.ListenerHelper;
import com.xiaoxin.update.helper.UpdateStatusChangeObserver;
import com.xiaoxin.update.listener.OnPatchListener;
import com.xiaoxin.update.listener.UpdateStatus;
import com.xiaoxin.update.util.UpdateLog;

/**
 * Created by liyuanbiao on 2017/9/6.
 */

public class PatchTask implements Runnable {
    private final UpdateStatusChangeObserver statusChangeObserver;
    private String oldFile;
    private String newFile;
    private String patchFile;
    private PatchUrl patchUrl;
    private OnPatchListener onPatchListener;

    public void setPatchUrl(PatchUrl patchUrl) {
        this.patchUrl = patchUrl;
    }

    public PatchUrl getPatchUrl() {
        return patchUrl;
    }

    public void setOnPatchListener(OnPatchListener onPatchListener) {
        this.onPatchListener = onPatchListener;
    }

    private void dispatchOnPrepare(PatchInfo patchInfo) {
        if (onPatchListener != null) {
            onPatchListener.onPrepare(patchInfo);
        }
    }

    private void dispatchOnComplete(PatchInfo patchInfo) {
        if (onPatchListener != null) {
            onPatchListener.onComplete(patchInfo);
        }
    }

    private void dispatchOnError(Exception e) {
        if (onPatchListener != null) {
            onPatchListener.onError(e);
        }
    }

    public PatchInfo getPatchInfo() {
        return new PatchInfo(oldFile, newFile, patchFile);
    }

    public PatchTask(PatchInfo patchInfo) {
        this(patchInfo.getOldFile(), patchInfo.getNewFile(), patchInfo.getPatchFile());
    }

    public PatchTask(String oldFile, String newFile, String patchFile) {
        this.oldFile = oldFile;
        this.newFile = newFile;
        this.patchFile = patchFile;
        statusChangeObserver = ListenerHelper.getStatusChangeObserver();
    }

    @Override
    public void run() {
        UpdateLog.d("PatchTask run() called");
        try {
            UpdateLog.d("PatchTask run() prepare");
            PatchInfo patchInfo = getPatchInfo();
            dispatchOnPrepare(patchInfo);
            statusChangeObserver.onUpdateStatusChange(UpdateStatus.STATUS_PATCH_PREPARE);
            PatchUtils.patch(oldFile, newFile, patchFile);
            UpdateLog.d("PatchTask run() complete");
            dispatchOnComplete(patchInfo);
            statusChangeObserver.onUpdateStatusChange(UpdateStatus.STATUS_PATCH_COMPLETE);
        } catch (Exception e) {
            dispatchOnError(e);
            statusChangeObserver.onUpdateStatusChange(UpdateStatus.STATUS_PATCH_ERROR);
            UpdateLog.e("PatchTask run patch: ", e);
        }
    }

    public void patch() {
        run();
    }
}
