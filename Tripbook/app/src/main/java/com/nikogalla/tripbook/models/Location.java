package com.nikogalla.tripbook.models;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.nikogalla.tripbook.R;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Nicola on 2017-01-27.
 */
@IgnoreExtraProperties
public class Location implements Parcelable {
    public static final String LOCATION_TABLE_NAME = "locations";
    private final String TAG = Location.class.getSimpleName();
    private String key;
    public String address;
    public Double latitude;
    public Double longitude;
    public String name;
    public String description;
    public Map<String,Photo> photos = new HashMap<>();
    public Map<String,Comment> comments = new HashMap<>();
    public Map<String,Rate> rates = new HashMap<>();
    public String userId;

    public Location() {
    }

    public Location(String address, Double latitude, Double longitude, String name, String description,String userId) {
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.description = description;
        this.userId = userId;
    }

    public int describeContents() {
        return 0;
    }

    private Location(Parcel in) {
        address = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        name = in.readString();
        description = in.readString();
        readPhotoUrls(in);
        readComments(in);
        readRates(in);
        userId = in.readString();
        key = in.readString();
    }

    private void readPhotoUrls(Parcel in){
        final int N = in.readInt();
        for (int i=0; i<N; i++) {
            String key = in.readString();
            Photo photo = new Photo();
            photo.createdAt = in.readString();
            photo.url = in.readString();
            photo.userId = in.readString();
            photos.put(key, photo);
        }
    }

    private void readComments(Parcel in){
        final int N = in.readInt();
        for (int i=0; i<N; i++) {
            String key = in.readString();
            Comment comment = new Comment();
            comment.createdAt = in.readString();
            comment.text = in.readString();
            comment.userId = in.readString();
            comment.userName = in.readString();
            comment.userPictureUrl = in.readString();
            comments.put(key, comment);
        }
    }

    private void readRates(Parcel in){
        final int N = in.readInt();
        for (int i=0; i<N; i++) {
            String key = in.readString();
            Rate rate = new Rate();
            rate.createdAt = in.readString();
            rate.rate = in.readInt();
            rate.userId = in.readString();
            rates.put(key, rate);
        }
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

    public void setKey (String key){
        this.key = key;
    }

    public String getKey (){
        return key;
    }

    @Override
    public void writeToParcel(Parcel out, int i) {
        out.writeString(address);
        out.writeDouble(latitude);
        out.writeDouble(longitude);
        out.writeString(name);
        out.writeString(description);
        writePhotoUrls(out);
        writeComments(out);
        writeRates(out);
        out.writeString(userId);
        out.writeString(key);
    }

    private void writePhotoUrls(Parcel out){
        final int N = photos.size();
        out.writeInt(N);
        if (N > 0) {
            for (Map.Entry<String, Photo> entry : photos.entrySet()) {
                out.writeString(entry.getKey());
                Photo dat = entry.getValue();
                out.writeString(dat.createdAt);
                out.writeString(dat.url);
                out.writeString(dat.userId);
                // etc...
            }
        }
    }

    private void writeComments(Parcel out){
        final int N = comments.size();
        out.writeInt(N);
        if (N > 0) {
            for (Map.Entry<String, Comment> entry : comments.entrySet()) {
                out.writeString(entry.getKey());
                Comment dat = entry.getValue();
                out.writeString(dat.createdAt);
                out.writeString(dat.text);
                out.writeString(dat.userId);
                out.writeString(dat.userName);
                out.writeString(dat.userPictureUrl);
            }
        }
    }

    private void writeRates(Parcel out){
        final int N = rates.size();
        out.writeInt(N);
        if (N > 0) {
            for (Map.Entry<String, Rate> entry : rates.entrySet()) {
                out.writeString(entry.getKey());
                Rate dat = entry.getValue();
                out.writeString(dat.createdAt);
                out.writeInt(dat.rate);
                out.writeString(dat.userId);
            }
        }
    }

    public String getMainPhotoUrl(){
        for (Map.Entry<String,Photo> photo : photos.entrySet()) {
            return photo.getValue().url;
        }
        return null;
    }

    public String getRateString(Context context){
        float locationRate = getRate();
        String locationRateString = String.format(Locale.getDefault(),"%.1f", locationRate)
                + "/" + String.valueOf(context.getResources().getInteger(R.integer.max_rate))
                + " (" + String.valueOf(rates.size()) + " " + context.getString(R.string.vote) + ")";
                ;
        return locationRateString;
    }
    public float getRate(){
        try{
            float locationRate;
            float totalRateValue = 0;
            for (Map.Entry<String,Rate> r : rates.entrySet()) {
                totalRateValue+= r.getValue().rate;
            }
            if(totalRateValue<=0)
                return 0;
            else{
                locationRate = totalRateValue/rates.size();
                return locationRate;
            }
        }catch (Exception e){
            Log.d(TAG,e.getMessage());
            return -1;
        }
    }

    public Map<String, Photo> getPhotos() {
        return photos;
    }

    public void setPhotos(Map<String, Photo> photos) {
        this.photos = photos;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("address", address);
        result.put("latitude", latitude);
        result.put("longitude", longitude);
        result.put("name", name);
        result.put("description", description);
        result.put("userId", userId);
        result.put("photos", photos);
        return result;
    }
}
