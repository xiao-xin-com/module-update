package com.xiaoxin.update.exception;

import android.content.ComponentName;

/**
 * Created by liyuanbiao on 2017/9/6.
 */
//服务异常断开异常
public class ServiceConnectedException extends Exception {
    private ComponentName componentName;

    public ServiceConnectedException(ComponentName name) {
        this.componentName = name;
    }

    public ServiceConnectedException(String message, ComponentName name) {
        super(message);
        this.componentName = name;
    }

    public ComponentName getComponentName() {
        return componentName;
    }

    public void setComponentName(ComponentName componentName) {
        this.componentName = componentName;
    }

    @Override
    public String toString() {
        return "ServiceConnectedException{" +
                "componentName=" + componentName +
                '}';
    }
}
