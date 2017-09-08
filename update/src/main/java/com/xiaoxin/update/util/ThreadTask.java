package com.xiaoxin.update.util;

import android.support.annotation.NonNull;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by liyuanbiao on 2017/9/8.
 */

public class ThreadTask {
    private static final ExecutorService threadPool
            = Executors.newCachedThreadPool();

    public static <T> Future<T> submit(@NonNull Callable<T> task) {
        return threadPool.submit(task);
    }

    public static <T> Future<T> submit(@NonNull Runnable task, T result) {
        return threadPool.submit(task, result);
    }

    public static Future<?> submit(@NonNull Runnable task) {
        return threadPool.submit(task);
    }

    public static void execute(@NonNull Runnable command) {
        threadPool.execute(command);
    }

    public static ExecutorService getThreadPool() {
        return threadPool;
    }
}
