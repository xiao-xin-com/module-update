package com.xiaoxin.update.bean;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Parcel;
import android.os.Parcelable;

import com.xiaoxin.update.UpdateManager;

/**
 * Created by liyuanbiao on 2017/9/7.
 */

public class AppInfo implements Parcelable {
    private int applicationIcon;
    private String applicationLabel;

    public AppInfo(int applicationIcon, String applicationLabel) {
        this.applicationIcon = applicationIcon;
        this.applicationLabel = applicationLabel;
    }

    public AppInfo(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
            applicationIcon = UpdateManager.getIcon();
            applicationLabel = (String) packageManager.getApplicationLabel(applicationInfo);
        } catch (PackageManager.NameNotFoundException e) {
            applicationIcon = android.R.drawable.sym_def_app_icon;
            applicationLabel = context.getPackageName();
        }
    }

    public int getApplicationIcon() {
        return applicationIcon;
    }

    public void setApplicationIcon(int applicationIcon) {
        this.applicationIcon = applicationIcon;
    }

    public String getApplicationLabel() {
        return applicationLabel;
    }

    public void setApplicationLabel(String applicationLabel) {
        this.applicationLabel = applicationLabel;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.applicationIcon);
        dest.writeString(this.applicationLabel);
    }

    protected AppInfo(Parcel in) {
        this.applicationIcon = in.readInt();
        this.applicationLabel = in.readString();
    }

    public static final Parcelable.Creator<AppInfo> CREATOR = new Parcelable.Creator<AppInfo>() {
        @Override
        public AppInfo createFromParcel(Parcel source) {
            return new AppInfo(source);
        }

        @Override
        public AppInfo[] newArray(int size) {
            return new AppInfo[size];
        }
    };
}
