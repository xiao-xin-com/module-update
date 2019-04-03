package com.xiaoxin.bootloader.ui.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.cundong.utils.PatchUtils;
import com.xiaoxin.bootloader.R;
import com.xiaoxin.bootloader.ui.activity.base.XXBaseActivity;

/**
 * Created by liyuanbiao on 2017/9/6.
 */

public class SmartUpdateActivity extends XXBaseActivity {
    private TextView md5View;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_info);
        md5View = (TextView) findViewById(R.id.md5_tv);
    }

    public void patch(View view) {
        new Thread() {
            public ProgressDialog progressDialog;

            @Override
            public void run() {
                super.run();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog = ProgressDialog.show(SmartUpdateActivity.this, "patch", "请稍候！！！");
                    }
                });
                PatchUtils.patch("/sdcard/old.apk", "/sdcard/new.apk", "/sdcard/p.patch");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                        }
                    }
                });

            }
        }.start();
    }

    public void oldFile(View view) {

    }

    public void patchFile(View view) {

    }

    public void newFile(View view) {

    }
}
