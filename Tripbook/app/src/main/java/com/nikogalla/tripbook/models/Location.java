package com.nikogalla.tripbook.models;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.gson.annotations.SerializedName;
import com.nikogalla.tripbook.R;

import java.util.ArrayList;
import java.util.Locale;
import java.util.StringTokenizer;

/**
 * Created by Nicola on 2017-01-27.
 */
@IgnoreExtraProperties
public class Location implements Parcelable {
    private final String TAG = Location.class.getSimpleName();
    public int id;
    public String address;
    public Double latitude;
    public Double longitude;
    public String name;
    public String description;
    public ArrayList<String> photoUrls;
    public ArrayList<Comment> comments;
    public ArrayList<Rate> rates;

    public String userId;

    public Location() {
    }

    public Location(String address, int id, Double latitude, Double longitude, String name, String description, ArrayList<String> photoUrls,ArrayList<Comment> comments, ArrayList<Rate> rates, String userId) {
        this.address = address;
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.description = description;
        this.photoUrls = photoUrls;
        this.comments = comments;
        this.rates = rates;
        this.userId = userId;
    }

    public int describeContents() {
        return 0;
    }

    private Location(Parcel in) {
        id = in.readInt();
        address = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        name = in.readString();
        description = in.readString();
        photoUrls = new ArrayList<String>();
        in.readStringList(photoUrls);
        comments = new ArrayList<>();
        in.readTypedList(comments,Comment.CREATOR);
        rates = new ArrayList<>();
        in.readTypedList(rates,Rate.CREATOR);
        userId = in.readString();
    }

    public static final Parcelable.Creator<Location> CREATOR
            = new Parcelable.Creator<Location>() {
        public Location createFromParcel(Parcel in) {
            return new Location(in);
        }

        public Location[] newArray(int size) {
            return new Location[size];
        }
    };


    @Override
    public void writeToParcel(Parcel out, int i) {
        out.writeInt(id);
        out.writeString(address);
        out.writeDouble(latitude);
        out.writeDouble(longitude);
        out.writeString(name);
        out.writeString(description);
        out.writeStringList(photoUrls);
        out.writeTypedList(comments);
        out.writeTypedList(rates);
        out.writeString(userId);
    }

    public String getRateString(Context context){
        float locationRate;
        int totalRateValue = 0;
        for (Rate r: rates){
            totalRateValue+= r.rate;
        }
        locationRate = Math.round(totalRateValue/rates.size());
        String locationRateString = String.format(Locale.getDefault(),"%.1f", locationRate)
                + "/" + String.valueOf(context.getResources().getInteger(R.integer.max_rate))
                + " (" + String.valueOf(rates.size()) + " " + context.getString(R.string.vote) + ")";
                ;
        return locationRateString;
    }
    public float getRate(){
        try{
            float locationRate;
            int totalRateValue = 0;
            for (Rate r: rates){
                totalRateValue+= r.rate;
            }
            locationRate = Math.round(totalRateValue/rates.size());
            return locationRate;
        }catch (Exception e){
            Log.d(TAG,e.getMessage());
            return -1;
        }
    }
}
