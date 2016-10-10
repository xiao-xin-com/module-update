package com.xiaoxin.update.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by liyuanbiao on 2016/9/23.
 */

public class XXRspUpdate implements Parcelable {

    /**
     * updateInfo : ["我是测试1","我是测试2","我是测试3","我是测试4",""]
     * versionCode : 2
     * packageName : com.xiaoxin.smallapple
     * updateUrl : http://www.baidu.com
     * platform : Android
     * isCurrent : true
     * createdAt : 2016-09-24T06:57:02.074Z
     * updatedAt : 2016-09-24T06:57:14.284Z
     * id : 57e623be2ab2f7bc4d60bebb
     */

    private String versionCode;
    private String packageName;
    private String updateUrl;
    private String platform;
    private boolean isCurrent;
    private String createdAt;
    private String updatedAt;
    private String id;
    private List<String> updateInfo;

    public String getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(String versionCode) {
        this.versionCode = versionCode;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getUpdateUrl() {
        return updateUrl;
    }

    public void setUpdateUrl(String updateUrl) {
        this.updateUrl = updateUrl;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public boolean isIsCurrent() {
        return isCurrent;
    }

    public void setIsCurrent(boolean isCurrent) {
        this.isCurrent = isCurrent;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getUpdateInfo() {
        return updateInfo;
    }

    public void setUpdateInfo(List<String> updateInfo) {
        this.updateInfo = updateInfo;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.versionCode);
        dest.writeString(this.packageName);
        dest.writeString(this.updateUrl);
        dest.writeString(this.platform);
        dest.writeByte(this.isCurrent ? (byte) 1 : (byte) 0);
        dest.writeString(this.createdAt);
        dest.writeString(this.updatedAt);
        dest.writeString(this.id);
        dest.writeStringList(this.updateInfo);
    }

    public XXRspUpdate() {
    }

    protected XXRspUpdate(Parcel in) {
        this.versionCode = in.readString();
        this.packageName = in.readString();
        this.updateUrl = in.readString();
        this.platform = in.readString();
        this.isCurrent = in.readByte() != 0;
        this.createdAt = in.readString();
        this.updatedAt = in.readString();
        this.id = in.readString();
        this.updateInfo = in.createStringArrayList();
    }

    public static final Creator<XXRspUpdate> CREATOR = new Creator<XXRspUpdate>() {
        @Override
        public XXRspUpdate createFromParcel(Parcel source) {
            return new XXRspUpdate(source);
        }

        @Override
        public XXRspUpdate[] newArray(int size) {
            return new XXRspUpdate[size];
        }
    };

    @Override
    public String toString() {
        return "XXRspUpdate{" +
                "versionCode='" + versionCode + '\'' +
                ", packageName='" + packageName + '\'' +
                ", updateUrl='" + updateUrl + '\'' +
                ", platform='" + platform + '\'' +
                ", isCurrent=" + isCurrent +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                ", id='" + id + '\'' +
                ", updateInfo=" + updateInfo +
                '}';
    }
}
