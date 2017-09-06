package com.xiaoxin.update.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by liyuanbiao on 2016/9/17.
 */

public class VersionInfo implements Parcelable {

    private String packageName;
    private int versionCode;
    private String updateUrl;
    private String md5checksum;
    private String model;
    private PatchUrl patchUrl;
    private String installFilename;
    private List<String> updateInfo;
    private String detail;

    public VersionInfo(String packageName, int versionCode) {
        this.packageName = packageName;
        this.versionCode = versionCode;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getUpdateUrl() {
        return updateUrl;
    }

    public void setUpdateUrl(String updateUrl) {
        this.updateUrl = updateUrl;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getInstallFilename() {
        return installFilename;
    }

    public void setInstallFilename(String installFilename) {
        this.installFilename = installFilename;
    }

    public PatchUrl getPatchUrl() {
        return patchUrl;
    }

    public void setPatchUrl(PatchUrl patchUrl) {
        this.patchUrl = patchUrl;
    }

    public String getMd5checksum() {
        return md5checksum;
    }

    public void setMd5checksum(String md5checksum) {
        this.md5checksum = md5checksum;
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
        dest.writeString(this.detail);
        dest.writeInt(this.versionCode);
        dest.writeString(this.updateUrl);
        dest.writeString(this.model);
        dest.writeString(this.packageName);
        dest.writeString(this.installFilename);
        dest.writeParcelable(this.patchUrl, flags);
        dest.writeString(this.md5checksum);
        dest.writeStringList(this.updateInfo);
    }

    public VersionInfo() {
    }

    protected VersionInfo(Parcel in) {
        this.detail = in.readString();
        this.versionCode = in.readInt();
        this.updateUrl = in.readString();
        this.model = in.readString();
        this.packageName = in.readString();
        this.installFilename = in.readString();
        this.patchUrl = in.readParcelable(PatchUrl.class.getClassLoader());
        this.md5checksum = in.readString();
        this.updateInfo = in.createStringArrayList();
    }

    public static final Parcelable.Creator<VersionInfo> CREATOR = new Parcelable.Creator<VersionInfo>() {
        @Override
        public VersionInfo createFromParcel(Parcel source) {
            return new VersionInfo(source);
        }

        @Override
        public VersionInfo[] newArray(int size) {
            return new VersionInfo[size];
        }
    };

    @Override
    public String toString() {
        return "VersionInfo{" +
                "detail='" + detail + '\'' +
                ", versionCode=" + versionCode +
                ", updateUrl='" + updateUrl + '\'' +
                ", model='" + model + '\'' +
                ", packageName='" + packageName + '\'' +
                ", installFilename='" + installFilename + '\'' +
                ", patchUrl=" + patchUrl +
                ", md5checksum='" + md5checksum + '\'' +
                ", updateInfo=" + updateInfo +
                '}';
    }
}
