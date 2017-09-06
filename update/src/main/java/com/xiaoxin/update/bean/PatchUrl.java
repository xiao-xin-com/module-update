package com.xiaoxin.update.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class PatchUrl implements Parcelable {
    private String url;
    private String md5;

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getMd5() {
        return md5;
    }

    @Override
    public String toString() {
        return "PatchUrl{" +
                "url='" + url + '\'' +
                ", md5='" + md5 + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.url);
        dest.writeString(this.md5);
    }

    public PatchUrl() {
    }

    protected PatchUrl(Parcel in) {
        this.url = in.readString();
        this.md5 = in.readString();
    }

    public static final Parcelable.Creator<PatchUrl> CREATOR = new Parcelable.Creator<PatchUrl>() {
        @Override
        public PatchUrl createFromParcel(Parcel source) {
            return new PatchUrl(source);
        }

        @Override
        public PatchUrl[] newArray(int size) {
            return new PatchUrl[size];
        }
    };
}
