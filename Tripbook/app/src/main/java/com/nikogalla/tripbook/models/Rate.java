package com.nikogalla.tripbook.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nicola on 2017-01-27.
 */

public class Rate implements Parcelable {
    public static final String RATES_TABLE_NAME = "rates";
    public String key;
    public String createdAt;
    public int rate;
    public String userId;
    public String userName;
    public String userPictureUrl;

    public Rate() {
    }

    private Rate(Parcel in) {
        createdAt = in.readString();
        rate = in.readInt();
        userId = in.readString();
        userName = in.readString();
        userPictureUrl = in.readString();
    }

    public static final Parcelable.Creator<Rate> CREATOR
            = new Parcelable.Creator<Rate>() {
        public Rate createFromParcel(Parcel in) {
            return new Rate(in);
        }

        public Rate[] newArray(int size) {
            return new Rate[size];
        }
    };

    public Rate(int rate,String createdAt,  String userId, String userName,String userPictureUrl) {
        this.createdAt = createdAt;
        this.rate = rate;
        this.userId = userId;
        this.userName = userName;
        this.userPictureUrl = userPictureUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(createdAt);
        out.writeInt(rate);
        out.writeString(userId);
        out.writeString(userName);
        out.writeString(userPictureUrl);
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("createdAt", createdAt);
        result.put("rate", rate);
        result.put("userId", userId);
        result.put("userName", userName);
        result.put("userPictureUrl", userPictureUrl);
        return result;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
