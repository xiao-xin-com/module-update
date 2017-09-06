package com.xiaoxin.update;

import android.app.Application;

import com.xiaoxin.update.config.UpdateConfiguration;

/**
 * Created by liyuanbiao on 2016/9/17.
 */

public abstract class UpdateApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        UpdateManager.init(this, getUpdateConfiguration());
    }

    protected abstract UpdateConfiguration getUpdateConfiguration();
}
