package com.xiaoxin.bootloader.params;

import android.os.Environment;

import com.xiaoxin.bootloader.util.XXFileUtil;

/**
 * Created by liyuanbiao on 2016/9/17.
 */

public class XXConfig {
    public static final String APK_DOWNLOAD_PATH = XXFileUtil.getFile
            (Environment.getExternalStorageDirectory(), "XiaoXin", "app").getAbsolutePath();
}
