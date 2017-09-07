package com.xiaoxin.update.service;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;

import com.xiaoxin.update.UpdateManager;
import com.xiaoxin.update.helper.CurrentStatus;
import com.xiaoxin.update.helper.ListenerHelper;
import com.xiaoxin.update.helper.UpdateStatusChangeObserver;
import com.xiaoxin.update.listener.UpdateStatus;
import com.xiaoxin.update.task.check.CheckVersion;
import com.xiaoxin.update.util.NetUtil;
import com.xiaoxin.update.util.UpdateLog;

import java.text.SimpleDateFormat;
import java.util.Date;

public class UpdateService extends Service {

    public static final String ACTION_CHECK_UPDATE = "com.xiaoxin.update.action.service";
    //是否是由闹钟启动的
    public static final String EXTRA_IS_ALARM = "isAlarm";

//    public static final long INTERNAL_CHECK_TIME = AlarmManager.INTERVAL_HALF_DAY;
//    public static final int DELAY_CHECK_TIME = (int) (1000 * 60 * 60 * 0.5);

//    public static final long INTERNAL_CHECK_TIME = AlarmManager.INTERVAL_FIFTEEN_MINUTES / 5;
//    public static final int DELAY_CHECK_TIME = (int) (1000 * 60);

    //接收检测更新的receiver();
    private UpdateReceiver updateReceiver;

    private UpdateStatusChangeObserver statusChangeObserver = ListenerHelper.getStatusChangeObserver();

    private CheckVersion checkVersion;

    private UpdateBinder updateBinder = new UpdateBinder();

    //有activity绑定时，如果是提示升级，而且版本信息下载完了，则去显示Dialog
    @Override
    public IBinder onBind(Intent intent) {
        UpdateLog.d("onBind() called with: intent = [" + intent + "]");
        return updateBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        UpdateLog.d("onUnbind() called with: intent = [" + intent + "]");
        return super.onUnbind(intent);
    }

    public class UpdateBinder extends Binder {
        public int getStatus() {
            return UpdateService.this.getStatus();
        }
    }

    @Override
    public void onCreate() {
        UpdateLog.d("onCreate() called");
        super.onCreate();
        statusChange(UpdateStatus.STATUS_NONE);
        checkVersion = new CheckVersion(this);
        registerUpdateReceiver();
        initRepeatingCheck();
    }

    private void initRepeatingCheck() {
        UpdateLog.d("initRepeatingCheck() called");
        long checkSpan = UpdateManager.getCheckSpan();
        UpdateLog.d("initRepeatingCheck: checkSpan -> " + checkSpan);
        if (checkSpan > 0) {
            setRepeatingCheck();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        UpdateLog.d("onStartCommand() called with: intent = [" + intent + "], flags = [" + flags + "], startId = [" + startId + "]");
        boolean isAlarm = intent != null && intent.getBooleanExtra(EXTRA_IS_ALARM, false);
        UpdateLog.d("onStartCommand: isAlarm -> " + isAlarm);
        if (isAlarm || startId == 1) {
            boolean netAvailable = NetUtil.isAvailable(getApplicationContext());
            UpdateLog.d("onStartCommand: netAvailable -> " + netAvailable);
            if (netAvailable) {
                check();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @SuppressLint("SimpleDateFormat")
    private void setRepeatingCheck() {
        UpdateLog.d("setRepeatingCheck() called");
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getApplicationContext(), UpdateService.class);
        intent.putExtra(EXTRA_IS_ALARM, true);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //取消掉之前的，防止重复
        alarmManager.cancel(pendingIntent);
        long intervalMillis = UpdateManager.getCheckSpan();
        long triggerAtMillis = System.currentTimeMillis() + intervalMillis;
        UpdateLog.d("setRepeatingCheck: 下次触发时间 " + new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date(triggerAtMillis)));
        //api19之后不精确，但是更新间隔不需要它精确
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis, intervalMillis, pendingIntent);
    }

    /**
     * 注册更新请求和网络状态改变的广播
     */
    private void registerUpdateReceiver() {
        if (updateReceiver == null) {
            UpdateLog.d("registerUpdateReceiver() called");
            updateReceiver = new UpdateReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ACTION_CHECK_UPDATE);
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            registerReceiver(updateReceiver, intentFilter);
        }
    }

    /**
     * 取消注册广播
     */
    private void unRegisterUpdateReceiver() {
        if (updateReceiver != null) {
            UpdateLog.d("unRegisterUpdateReceiver() called");
            unregisterReceiver(updateReceiver);
            updateReceiver = null;
        }
    }

    private void statusChange(int status) {
        statusChangeObserver.onUpdateStatusChange(status);
    }

    public int getStatus() {
        return CurrentStatus.getStatus();
    }

    @Override
    public void onDestroy() {
        UpdateLog.d("onDestroy() called");
        super.onDestroy();
        checkVersion.release();
        unRegisterUpdateReceiver();
    }

    private class UpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            UpdateLog.d("onReceive: action -> " + action);
            if (TextUtils.equals(action, UpdateService.ACTION_CHECK_UPDATE)) {
                if (NetUtil.isAvailable(context)) {
                    check();
                }
            } else if (TextUtils.equals(action, ConnectivityManager.CONNECTIVITY_ACTION)) {
                UpdateLog.d("onReceive: first -> " + checkVersion.isFirst());
                if (NetUtil.isAvailable(context) && checkVersion.isFirst()) {
                    check();
                }
            }
        }
    }

    private void check() {
        UpdateLog.d("check() called start");
        //如果升级请求没结束，不再发起第二次请求
        boolean checking = checkVersion.isChecking();
        UpdateLog.d("check() called checking -> " + checking);
        if (checking) return;
        //从网络上获取最新的版本信息
        checkVersion.check();
    }

}
