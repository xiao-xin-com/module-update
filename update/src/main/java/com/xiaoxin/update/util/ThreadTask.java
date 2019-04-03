package com.xiaoxin.update.util;

import android.support.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by liyuanbiao on 2017/9/8.
 */

public final class ThreadTask {
    private ThreadTask() {
        throw new IllegalAccessError("ThreadTask 不能被实例化");
    }

    private static final Executor EXECUTOR = new Executor() {
        private final Executor executor =
                Executors.newCachedThreadPool();

        @Override
        public void execute(Runnable command) {
            executor.execute(command);
        }
    };

    public static void execute(@NonNull Runnable task) {
        EXECUTOR.execute(task);
    }

    public static Executor getExecutor() {
        return EXECUTOR;
    }
}
