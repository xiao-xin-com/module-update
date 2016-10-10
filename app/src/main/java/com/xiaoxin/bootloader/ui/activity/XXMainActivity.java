package com.xiaoxin.bootloader.ui.activity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiaoxin.bootloader.R;
import com.xiaoxin.bootloader.ui.activity.base.XXBaseActivity;
import com.xiaoxin.update.XXUpdateManager;


public class XXMainActivity extends XXBaseActivity {

    private ImageView mAppLogo;
    private TextView mUpdateStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        XXUpdateManager.check(this);
    }

    private void initView() {
        mAppLogo = (ImageView) findViewById(R.id.app_logo);
        mUpdateStatus = (TextView) findViewById(R.id.update_status);
    }

    @Override
    protected void onResume() {
        super.onResume();
        XXUpdateManager.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        XXUpdateManager.onPause(this);
    }
}
