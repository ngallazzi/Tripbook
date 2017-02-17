package com.nikogalla.tripbook.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nicola on 2017-01-30.
 */

public class Photo implements Parcelable{
    public String key;
    public String createdAt;
    public String url;
    public String userId;

    public Photo() {
    }

    private Photo(Parcel in) {
        createdAt = in.readString();
        url = in.readString();
        userId = in.readString();
    }

    public static final Parcelable.Creator<Photo> CREATOR
            = new Parcelable.Creator<Photo>() {
        public Photo createFromParcel(Parcel in) {
            return new Photo(in);
        }

        public Photo[] newArray(int size) {
            return new Photo[size];
        }
    };

    public Photo(String url,String createdAt,  String userId) {
        this.createdAt = createdAt;
        this.url = url;
        this.userId = userId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(createdAt);
        out.writeString(url);
        out.writeString(userId);
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("createdAt", createdAt);
        result.put("url", url);
        result.put("userId", userId);
        return result;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
