package com.xiaoxin.update.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by liyuanbiao on 2017/9/6.
 */

public class PatchInfo implements Parcelable {
    private String oldFile;
    private String newFile;
    private String patchFile;

    public PatchInfo(String oldFile, String newFile, String patchFile) {
        this.oldFile = oldFile;
        this.newFile = newFile;
        this.patchFile = patchFile;
    }

    public PatchInfo() {
    }

    public String getOldFile() {
        return oldFile;
    }

    public void setOldFile(String oldFile) {
        this.oldFile = oldFile;
    }

    public String getNewFile() {
        return newFile;
    }

    public void setNewFile(String newFile) {
        this.newFile = newFile;
    }

    public String getPatchFile() {
        return patchFile;
    }

    public void setPatchFile(String patchFile) {
        this.patchFile = patchFile;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.oldFile);
        dest.writeString(this.newFile);
        dest.writeString(this.patchFile);
    }

    protected PatchInfo(Parcel in) {
        this.oldFile = in.readString();
        this.newFile = in.readString();
        this.patchFile = in.readString();
    }

    public static final Parcelable.Creator<PatchInfo> CREATOR = new Parcelable.Creator<PatchInfo>() {
        @Override
        public PatchInfo createFromParcel(Parcel source) {
            return new PatchInfo(source);
        }

        @Override
        public PatchInfo[] newArray(int size) {
            return new PatchInfo[size];
        }
    };
}
