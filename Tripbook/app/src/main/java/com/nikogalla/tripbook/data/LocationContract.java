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

import java.math.BigDecimal;
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
        public static final String COLUMN_PICTURE_URL = "picture_url";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_DISTANCE = "distance";
        public static final String COLUMN_COMMENT_COUNT = "comments_count";
        public static final String COLUMN_RATE = "rate";
        public static final String COLUMN_RATE_COUNT = "rate_count";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_IS_FAVORITE = "is_favorite";

        public static Uri buildLocationUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        // Useful to fetch a specific row inside the table
        public static Uri buildLocationUriWithLocationKey(String key) {
            Uri uri = CONTENT_URI.buildUpon()
                    .appendPath(key)
                    .build();
            return uri;
        }

        public static String getLocationKeyFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }
}
