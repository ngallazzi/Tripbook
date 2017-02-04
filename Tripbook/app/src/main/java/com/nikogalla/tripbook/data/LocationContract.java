package com.nikogalla.tripbook.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.Time;

import com.nikogalla.tripbook.models.Comment;
import com.nikogalla.tripbook.models.Location;
import com.nikogalla.tripbook.models.Photo;
import com.nikogalla.tripbook.models.Rate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Nicola on 2017-02-03.
 */

public class LocationContract {
    public static final String CONTENT_AUTHORITY = "com.nikogalla.tripbook";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_LOCATION = "location";

    public static final class LocationEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_LOCATION).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION;

        // Table name
        public static final String TABLE_NAME = "location";

        public static final String COLUMN_KEY = "key";
        public static final String COLUMN_ADDRESS = "address";
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_LONGITUDE = "longitude";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_USER_ID = "user_id";

        public static Uri buildLocationUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static void saveLocationLocally(Location location, Context context){
            // Table location
            ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_KEY, location.getKey());
            contentValues.put(COLUMN_ADDRESS, location.address);
            contentValues.put(COLUMN_LATITUDE, location.latitude);
            contentValues.put(COLUMN_LONGITUDE, location.longitude);
            contentValues.put(COLUMN_NAME, location.name);
            contentValues.put(COLUMN_DESCRIPTION, location.description);
            contentValues.put(COLUMN_USER_ID, location.userId);
            context.getContentResolver().insert(LocationContract.LocationEntry.CONTENT_URI, contentValues);
        }
    }

    public static final String PATH_COMMENT = "comment";

    public static final class CommentEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_COMMENT).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_COMMENT;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_COMMENT;

        // Table name
        public static final String TABLE_NAME = "comment";

        public static final String COLUMN_KEY = "key";
        public static final String COLUMN_CREATED_AT = "created_at";
        public static final String COLUMN_TEXT = "text";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_USER_NAME = "user_name";
        public static final String COLUMN_USER_PICTURE_URL = "user_picture_url";
        public static final String COLUMN_LOCATION_KEY = "location_key";

        public static Uri buildCommentUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static void saveLocationCommentsLocally(Location location, Context context){
            // Table comments
            for(Map.Entry<String, Comment> entry : location.comments.entrySet()) {
                Comment comment = entry.getValue();
                ContentValues contentValues = new ContentValues();
                contentValues.put(COLUMN_KEY, comment.getKey());
                contentValues.put(COLUMN_CREATED_AT, comment.createdAt);
                contentValues.put(COLUMN_TEXT, comment.text);
                contentValues.put(COLUMN_USER_ID, comment.userId);
                contentValues.put(COLUMN_USER_NAME, comment.userName);
                contentValues.put(COLUMN_USER_PICTURE_URL, comment.userPictureUrl);
                contentValues.put(COLUMN_LOCATION_KEY, location.getKey());
                context.getContentResolver().insert(LocationContract.CommentEntry.CONTENT_URI, contentValues);
            }
        }
    }

    public static final String PATH_PHOTO = "photo";

    public static final class PhotoEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PHOTO).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PHOTO;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PHOTO;

        // Table name
        public static final String TABLE_NAME = "photo";

        public static final String COLUMN_KEY = "key";
        public static final String COLUMN_CREATED_AT = "created_at";
        public static final String COLUMN_URL = "url";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_LOCATION_KEY = "location_key";

        public static Uri buildPhotoUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static void saveLocationPhotosLocally(Location location, Context context){
            // Table comments
            for(Map.Entry<String, Photo> entry : location.photos.entrySet()) {
                Photo photo = entry.getValue();
                ContentValues contentValues = new ContentValues();
                contentValues.put(COLUMN_KEY, photo.getKey());
                contentValues.put(COLUMN_CREATED_AT, photo.createdAt);
                contentValues.put(COLUMN_URL, photo.url);
                contentValues.put(COLUMN_USER_ID, photo.userId);
                contentValues.put(COLUMN_LOCATION_KEY, location.getKey());
                context.getContentResolver().insert(LocationContract.PhotoEntry.CONTENT_URI, contentValues);
            }
        }
    }

    public static final String PATH_RATE = "rate";

    public static final class RateEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_RATE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_RATE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_RATE;

        // Table name
        public static final String TABLE_NAME = "rate";

        public static final String COLUMN_KEY = "key";
        public static final String COLUMN_CREATED_AT = "created_at";
        public static final String COLUMN_RATE = "rate";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_USER_NAME = "user_name";
        public static final String COLUMN_USER_PICTURE_URL = "user_picture_url";

        public static Uri buildRateUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static void saveLocationRatesLocally(Location location, Context context){
            // Table comments
            for(Map.Entry<String, Rate> entry : location.rates.entrySet()) {
                Rate rate = entry.getValue();
                ContentValues contentValues = new ContentValues();
                contentValues.put(COLUMN_KEY, rate.getKey());
                contentValues.put(COLUMN_CREATED_AT, rate.createdAt);
                contentValues.put(COLUMN_RATE, rate.rate);
                contentValues.put(COLUMN_USER_ID, rate.userId);
                contentValues.put(COLUMN_USER_NAME, rate.userName);
                contentValues.put(COLUMN_USER_PICTURE_URL, rate.userPictureUrl);
                context.getContentResolver().insert(LocationContract.RateEntry.CONTENT_URI, contentValues);
            }
        }
    }

    public static long normalizeDate(long startDate) {
        // normalize the start date to the beginning of the (UTC) day
        Time time = new Time();
        time.set(startDate);
        int julianDay = Time.getJulianDay(startDate, time.gmtoff);
        return time.setJulianDay(julianDay);
    }
}
