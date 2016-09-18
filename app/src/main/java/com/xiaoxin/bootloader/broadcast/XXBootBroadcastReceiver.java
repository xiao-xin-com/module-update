package com.xiaoxin.bootloader.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.text.TextUtils;

import com.xiaoxin.bootloader.service.XXBootService;


public class XXBootBroadcastReceiver extends BroadcastReceiver {

    private static final String GPS_CHANGED_ACTION = LocationManager.PROVIDERS_CHANGED_ACTION;//"android.location.PROVIDERS_CHANGED";
    private static final String NET_CHANGED_ACTION = ConnectivityManager.CONNECTIVITY_ACTION;//"android.net.conn.CONNECTIVITY_CHANGE";

    public XXBootBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (TextUtils.equals(action, Intent.ACTION_BOOT_COMPLETED)) {
            startBootService(context);
        } else if (TextUtils.equals(action, Intent.ACTION_POWER_CONNECTED)) {

        } else if (TextUtils.equals(action, Intent.ACTION_POWER_DISCONNECTED)) {
            //DCApplication.deInitDCloud();

        } else if (NET_CHANGED_ACTION.equals(intent.getAction())) {
            //receive this for keep service alive background
            //open this if necessary
        }
    }

    private void startBootService(Context context) {
        Intent intent = new Intent(context, XXBootService.class);
        context.startService(intent);
    }
}
