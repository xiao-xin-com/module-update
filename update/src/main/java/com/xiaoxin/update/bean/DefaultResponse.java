package com.xiaoxin.update.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liyuanbiao on 2016/9/23.
 */

public class DefaultResponse implements Parcelable {

    private int versionCode;
    private String updateUrl;
    private String model;
    private String packageName;
    private String platform;
    private String installFilename;
    private boolean isCurrent;
    private String createdAt;
    private String updatedAt;
    private Map<String, PatchUrl> patchUrl;
    private String md5checksum;
    private String id;
    private List<String> updateInfo;

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

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getInstallFilename() {
        return installFilename;
    }

    public void setInstallFilename(String installFilename) {
        this.installFilename = installFilename;
    }

    public boolean isCurrent() {
        return isCurrent;
    }

    public void setCurrent(boolean current) {
        isCurrent = current;
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

    public Map<String, PatchUrl> getPatchUrl() {
        return patchUrl;
    }

    public void setPatchUrl(Map<String, PatchUrl> patchUrl) {
        this.patchUrl = patchUrl;
    }

    public String getMd5checksum() {
        return md5checksum;
    }

    public void setMd5checksum(String md5checksum) {
        this.md5checksum = md5checksum;
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
        dest.writeInt(this.versionCode);
        dest.writeString(this.updateUrl);
        dest.writeString(this.model);
        dest.writeString(this.packageName);
        dest.writeString(this.platform);
        dest.writeString(this.installFilename);
        dest.writeByte(this.isCurrent ? (byte) 1 : (byte) 0);
        dest.writeString(this.createdAt);
        dest.writeString(this.updatedAt);
        dest.writeInt(this.patchUrl.size());
        for (Map.Entry<String, PatchUrl> entry : this.patchUrl.entrySet()) {
            dest.writeString(entry.getKey());
            dest.writeParcelable(entry.getValue(), flags);
        }
        dest.writeString(this.md5checksum);
        dest.writeString(this.id);
        dest.writeStringList(this.updateInfo);
    }

    public DefaultResponse() {
    }

    protected DefaultResponse(Parcel in) {
        this.versionCode = in.readInt();
        this.updateUrl = in.readString();
        this.model = in.readString();
        this.packageName = in.readString();
        this.platform = in.readString();
        this.installFilename = in.readString();
        this.isCurrent = in.readByte() != 0;
        this.createdAt = in.readString();
        this.updatedAt = in.readString();
        int patchUrlSize = in.readInt();
        this.patchUrl = new HashMap<String, PatchUrl>(patchUrlSize);
        for (int i = 0; i < patchUrlSize; i++) {
            String key = in.readString();
            PatchUrl value = in.readParcelable(PatchUrl.class.getClassLoader());
            this.patchUrl.put(key, value);
        }
        this.md5checksum = in.readString();
        this.id = in.readString();
        this.updateInfo = in.createStringArrayList();
    }

    public static final Parcelable.Creator<DefaultResponse> CREATOR = new Parcelable.Creator<DefaultResponse>() {
        @Override
        public DefaultResponse createFromParcel(Parcel source) {
            return new DefaultResponse(source);
        }

        @Override
        public DefaultResponse[] newArray(int size) {
            return new DefaultResponse[size];
        }
    };

    @Override
    public String toString() {
        return "XXRspUpdate{" +
                "versionCode=" + versionCode +
                ", updateUrl='" + updateUrl + '\'' +
                ", model='" + model + '\'' +
                ", packageName='" + packageName + '\'' +
                ", platform='" + platform + '\'' +
                ", installFilename='" + installFilename + '\'' +
                ", isCurrent=" + isCurrent +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                ", patchUrl=" + patchUrl +
                ", md5checksum='" + md5checksum + '\'' +
                ", id='" + id + '\'' +
                ", updateInfo=" + updateInfo +
                '}';
    }
}
