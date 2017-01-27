package com.nikogalla.tripbook.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Nicola on 2017-01-27.
 */

public class Rate implements Parcelable {
    public String createdAt;
    public int rate;
    public String userId;

    public Rate() {
    }

    private Rate(Parcel in) {
        createdAt = in.readString();
        rate = in.readInt();
        userId = in.readString();
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(createdAt);
        out.writeInt(rate);
        out.writeString(userId);
    }
}
