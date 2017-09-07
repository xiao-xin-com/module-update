package com.xiaoxin.bootloader.ui.activity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiaoxin.bootloader.R;
import com.xiaoxin.bootloader.ui.activity.base.XXBaseActivity;
import com.xiaoxin.update.UpdateManager;
import com.xiaoxin.update.listener.OnConnectListener;


public class XXMainActivity extends XXBaseActivity {

    private ImageView mAppLogo;
    private TextView mUpdateStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        if (UpdateManager.isInit()) {
            UpdateManager.check(this);
        }
        UpdateManager.registerOnConnectListener(connectListener);
    }


    private OnConnectListener connectListener = new OnConnectListener() {
        @Override
        public void onConnected(Exception e) {
            UpdateManager.check(XXMainActivity.this);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UpdateManager.unregisterOnConnectListener(connectListener);
    }

    private void initView() {
        mAppLogo = (ImageView) findViewById(R.id.app_logo);
        mUpdateStatus = (TextView) findViewById(R.id.update_status);
    }

    @Override
    protected void onResume() {
        super.onResume();
        UpdateManager.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        UpdateManager.onPause(this);
    }
}
