package com.xiaoxin.update.task.install;

import android.content.Context;

import com.xiaoxin.update.util.UpdateUtil;

import java.io.File;

/**
 * Created by liyuanbiao on 2017/9/6.
 */

class SystemInstallTask extends InstallTask {
    private Context context;

    public SystemInstallTask(Context context, String filePath) {
        super(filePath);
        this.context = context;
    }

    @Override
    public void run() {
        UpdateUtil.startSystemInstall(context, new File(getFilePath()));
    }
}
