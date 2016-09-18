package com.xiaoxin.update.util;

import android.os.Handler;
import android.os.Looper;

/**
 * Created by liyuanbiao on 2016/9/18.
 */

public class XXUITask {
    private static final Handler handler = new Handler(Looper.getMainLooper());

    private XXUITask() {
    }

    public static void post(Runnable runnable) {
        handler.post(runnable);
    }

    public static void autoPost(Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            handler.post(runnable);
        }
    }
}
