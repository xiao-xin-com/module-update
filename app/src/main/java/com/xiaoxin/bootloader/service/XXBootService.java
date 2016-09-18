package com.xiaoxin.bootloader.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class XXBootService extends Service {
    public XXBootService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(this, "BootService start", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onCreate: ");
    }

    private static final String TAG = "BootService";
}
