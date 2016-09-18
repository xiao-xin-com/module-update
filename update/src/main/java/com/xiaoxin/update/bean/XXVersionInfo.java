package com.xiaoxin.update.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by liyuanbiao on 2016/9/17.
 */

public class XXVersionInfo implements Parcelable {
    private String packageName;
    private int versionCode;
    private String updateInfo;
    private String downloadUrl;


    public XXVersionInfo(String packageName, int versionCode, String updateInfo, String downloadUrl) {
        this.packageName = packageName;
        this.versionCode = versionCode;
        this.updateInfo = updateInfo;
        this.downloadUrl = downloadUrl;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getUpdateInfo() {
        return updateInfo;
    }

    public void setUpdateInfo(String updateInfo) {
        this.updateInfo = updateInfo;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.packageName);
        dest.writeInt(this.versionCode);
        dest.writeString(this.updateInfo);
        dest.writeString(this.downloadUrl);
    }

    public XXVersionInfo() {
    }

    protected XXVersionInfo(Parcel in) {
        this.packageName = in.readString();
        this.versionCode = in.readInt();
        this.updateInfo = in.readString();
        this.downloadUrl = in.readString();
    }

    public static final Parcelable.Creator<XXVersionInfo> CREATOR = new Parcelable.Creator<XXVersionInfo>() {
        @Override
        public XXVersionInfo createFromParcel(Parcel source) {
            return new XXVersionInfo(source);
        }

        @Override
        public XXVersionInfo[] newArray(int size) {
            return new XXVersionInfo[size];
        }
    };

    @Override
    public String toString() {
        return "XXVersionInfo{" +
                "packageName='" + packageName + '\'' +
                ", versionCode=" + versionCode +
                ", updateInfo='" + updateInfo + '\'' +
                ", downloadUrl='" + downloadUrl + '\'' +
                '}';
    }
}
