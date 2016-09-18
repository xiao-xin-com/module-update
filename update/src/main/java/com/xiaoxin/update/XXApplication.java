package com.xiaoxin.update;

import android.app.Application;

import com.xiaoxin.update.config.XXUpdateConfiguration;

/**
 * Created by liyuanbiao on 2016/9/17.
 */

public abstract class XXApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        XXUpdateManager.init(this, getUpdateConfiguration());
    }

    protected abstract XXUpdateConfiguration getUpdateConfiguration();
}
