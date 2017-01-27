package com.nikogalla.tripbook.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Nicola on 2017-01-27.
 */

public class Comment implements Parcelable {
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
}
