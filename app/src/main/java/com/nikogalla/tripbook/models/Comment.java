package com.nikogalla.tripbook.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nicola on 2017-01-27.
 */

public class Comment implements Parcelable {
    public static final String COMMENTS_TABLE_NAME = "comments";
    public String key;
    public String createdAt;
    public String text;
    public String userId;
    public String userName;
    public String userPictureUrl;

    private Comment(Parcel in) {
        createdAt = in.readString();
        text = in.readString();
        userId = in.readString();
        userName = in.readString();
        userPictureUrl = in.readString();
    }

    public Comment() {
    }

    public static final Parcelable.Creator<Comment> CREATOR
            = new Parcelable.Creator<Comment>() {
        public Comment createFromParcel(Parcel in) {
            return new Comment(in);
        }

        public Comment[] newArray(int size) {
            return new Comment[size];
        }
    };

    public Comment(String createdAt, String text, String userId, String userName, String userPictureUrl) {
        this.createdAt = createdAt;
        this.text = text;
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
        out.writeString(text);
        out.writeString(userId);
        out.writeString(userName);
        out.writeString(userPictureUrl);
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("createdAt", createdAt);
        result.put("text", text);
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
