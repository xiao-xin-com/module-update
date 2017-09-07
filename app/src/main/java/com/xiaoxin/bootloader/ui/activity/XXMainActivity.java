package com.xiaoxin.bootloader.ui.activity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiaoxin.bootloader.R;
import com.xiaoxin.bootloader.ui.activity.base.XXBaseActivity;
import com.xiaoxin.update.UpdateManager;
import com.xiaoxin.update.helper.ListenerHelper;
import com.xiaoxin.update.listener.OnConnectListener;
import com.xiaoxin.update.listener.OnUpdateStatusChangeListener;
import com.xiaoxin.update.util.GetAppInfo;


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

        ListenerHelper.registerUpdateStatusChangeListener(onUpdateStatusChangeListener);
    }

    private OnUpdateStatusChangeListener onUpdateStatusChangeListener = new OnUpdateStatusChangeListener() {
        @Override
        public void onUpdateStatusChange(final int status) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mUpdateStatus.append(String.valueOf(status) + " ");
                }
            });
        }
    };

    private OnConnectListener connectListener = new OnConnectListener() {
        @Override
        public void onConnected(Exception e) {
            if (e == null) {
                UpdateManager.check(XXMainActivity.this);
            } else {
                UpdateManager.reInit();
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UpdateManager.unregisterOnConnectListener(connectListener);
        ListenerHelper.unregisterUpdateStatusChangeListener(onUpdateStatusChangeListener);
    }

    private void initView() {
        mAppLogo = (ImageView) findViewById(R.id.app_logo);
        mUpdateStatus = (TextView) findViewById(R.id.update_status);
        TextView versionInfo = (TextView) findViewById(R.id.versionInfo);
        versionInfo.setText("versionName: " + GetAppInfo.getAppVersionName(this) +
                "\tversionCode: " + GetAppInfo.getAppVersionCode(this));
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
