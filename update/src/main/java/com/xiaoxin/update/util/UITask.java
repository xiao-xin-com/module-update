package com.xiaoxin.update.util;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;

/**
 * Created by liyuanbiao on 2016/9/18.
 */

public final class UITask {
    private UITask() {
        throw new IllegalAccessError("UITask 不能实例化");
    }

    private static final Executor MAIN_EXECUTOR = new Executor() {
        private final Handler handler =
                new Handler(Looper.getMainLooper());

        @Override
        public void execute(Runnable command) {
            handler.post(command);
        }
    };

    public static void post(Runnable command) {
        MAIN_EXECUTOR.execute(command);
    }

    public static void autoPost(Runnable command) {
        if (isUIThread()) {
            command.run();
        } else {
            post(command);
        }
    }

    private static boolean isUIThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }
}
