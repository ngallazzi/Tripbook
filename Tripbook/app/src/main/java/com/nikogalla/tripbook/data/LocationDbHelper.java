package com.nikogalla.tripbook.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.nikogalla.tripbook.data.LocationContract.LocationEntry;
import com.nikogalla.tripbook.models.Location;

import java.util.ArrayList;

/**
 * Created by Nicola on 2017-02-03.
 */

public class LocationDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 11;

    static final String DATABASE_NAME = "location.db";

    public LocationDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create a table to hold locations.
        final String SQL_CREATE_LOCATION_TABLE = "CREATE TABLE " + LocationEntry.TABLE_NAME + " (" +
                LocationEntry._ID + " INTEGER PRIMARY KEY," +
                LocationEntry.COLUMN_KEY + " TEXT UNIQUE ON CONFLICT REPLACE," +
                LocationEntry.COLUMN_ADDRESS + " TEXT NOT NULL, " +
                LocationEntry.COLUMN_LATITUDE + " REAL NOT NULL, " +
                LocationEntry.COLUMN_LONGITUDE + " REAL NOT NULL, " +
                LocationEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                LocationEntry.COLUMN_PICTURE_URL + " TEXT NOT NULL, " +
                LocationEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL, " +
                LocationEntry.COLUMN_DISTANCE + " REAL, " +
                LocationEntry.COLUMN_COMMENT_COUNT + " INTEGER, " +
                LocationEntry.COLUMN_RATE + " REAL, " +
                LocationEntry.COLUMN_RATE_COUNT + " INTEGER, " +
                LocationEntry.COLUMN_USER_ID + " TEXT NOT NULL " +
                " );";
        sqLiteDatabase.execSQL(SQL_CREATE_LOCATION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + LocationEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public static void saveLocationLocally(Location location, Context context){
        // Table location
        ContentValues contentValues = new ContentValues();
        contentValues.put(LocationEntry.COLUMN_KEY, location.getKey());
        contentValues.put(LocationEntry.COLUMN_ADDRESS, location.address);
        contentValues.put(LocationEntry.COLUMN_LATITUDE, location.latitude);
        contentValues.put(LocationEntry.COLUMN_LONGITUDE, location.longitude);
        contentValues.put(LocationEntry.COLUMN_NAME, location.name);
        contentValues.put(LocationEntry.COLUMN_PICTURE_URL, location.getMainPhotoUrl());
        contentValues.put(LocationEntry.COLUMN_DESCRIPTION, location.description);
        contentValues.put(LocationEntry.COLUMN_DISTANCE, location.distance);
        contentValues.put(LocationEntry.COLUMN_USER_ID, location.userId);
        contentValues.put(LocationEntry.COLUMN_COMMENT_COUNT, location.comments.size());
        contentValues.put(LocationEntry.COLUMN_RATE, location.getRate());
        contentValues.put(LocationEntry.COLUMN_RATE_COUNT, location.rates.size());
        context.getContentResolver().insert(LocationContract.LocationEntry.CONTENT_URI, contentValues);
    }


    public static int saveLocationsLocally(ArrayList<Location> locations, Context context){
        ArrayList<ContentValues> locationsToInsert = new ArrayList<>();
        int insertedRowsCount = -1;
        for (Location location : locations){
            ContentValues contentValues = new ContentValues();
            contentValues.put(LocationEntry.COLUMN_KEY, location.getKey());
            contentValues.put(LocationEntry.COLUMN_ADDRESS, location.address);
            contentValues.put(LocationEntry.COLUMN_LATITUDE, location.latitude);
            contentValues.put(LocationEntry.COLUMN_LONGITUDE, location.longitude);
            contentValues.put(LocationEntry.COLUMN_NAME, location.name);
            contentValues.put(LocationEntry.COLUMN_PICTURE_URL, location.getMainPhotoUrl());
            contentValues.put(LocationEntry.COLUMN_DESCRIPTION, location.description);
            contentValues.put(LocationEntry.COLUMN_DISTANCE, location.distance);
            contentValues.put(LocationEntry.COLUMN_USER_ID, location.userId);
            contentValues.put(LocationEntry.COLUMN_COMMENT_COUNT, location.comments.size());
            contentValues.put(LocationEntry.COLUMN_RATE, location.getRate());
            contentValues.put(LocationEntry.COLUMN_RATE_COUNT, location.rates.size());
            locationsToInsert.add(contentValues);
        }
        // Add products to the database
        ContentValues[] cvArray = new ContentValues[locationsToInsert.size()];
        locationsToInsert.toArray(cvArray);
        insertedRowsCount = context.getContentResolver().bulkInsert(LocationContract.LocationEntry.CONTENT_URI, cvArray);
        return insertedRowsCount;
    }
}
