package com.xiaoxin.update.listener;

import com.xiaoxin.update.bean.PatchInfo;

/**
 * Created by liyuanbiao on 2017/9/6.
 */

public interface OnPatchListener {
    void onPrepare(PatchInfo patchInfo);

    void onComplete(PatchInfo patchInfo);

    void onError(Exception e);
}
