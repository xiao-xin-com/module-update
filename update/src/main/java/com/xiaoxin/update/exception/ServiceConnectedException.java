package com.xiaoxin.update.exception;

import android.content.ComponentName;

/**
 * Created by liyuanbiao on 2017/9/6.
 */

public class ServiceConnectedException extends Exception {
    private ComponentName name;

    public ServiceConnectedException(ComponentName name) {
        this.name = name;
    }

    public ServiceConnectedException(String message, ComponentName name) {
        super(message);
        this.name = name;
    }
}
