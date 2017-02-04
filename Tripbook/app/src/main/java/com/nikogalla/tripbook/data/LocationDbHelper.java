package com.nikogalla.tripbook.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.nikogalla.tripbook.data.LocationContract.CommentEntry;
import com.nikogalla.tripbook.data.LocationContract.LocationEntry;
import com.nikogalla.tripbook.data.LocationContract.PhotoEntry;
import com.nikogalla.tripbook.data.LocationContract.RateEntry;

/**
 * Created by Nicola on 2017-02-03.
 */

public class LocationDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 4;

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
                LocationEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL, " +
                LocationEntry.COLUMN_USER_ID + " TEXT NOT NULL " +
                " );";
        sqLiteDatabase.execSQL(SQL_CREATE_LOCATION_TABLE);

        final String SQL_CREATE_COMMENT_TABLE = "CREATE TABLE " + CommentEntry.TABLE_NAME + " (" +
                CommentEntry._ID + " INTEGER PRIMARY KEY," +
                CommentEntry.COLUMN_KEY + " TEXT UNIQUE ON CONFLICT REPLACE," +
                CommentEntry.COLUMN_CREATED_AT + " INTEGER NOT NULL, " +
                CommentEntry.COLUMN_TEXT + " TEXT NOT NULL, " +
                CommentEntry.COLUMN_USER_ID + " TEXT NOT NULL, " +
                CommentEntry.COLUMN_USER_NAME + " TEXT NOT NULL, " +
                CommentEntry.COLUMN_USER_PICTURE_URL + " TEXT NOT NULL, " +
                CommentEntry.COLUMN_LOCATION_KEY + " TEXT NOT NULL " +
                " );";
        sqLiteDatabase.execSQL(SQL_CREATE_COMMENT_TABLE);

        final String SQL_CREATE_PHOTO_TABLE = "CREATE TABLE " + PhotoEntry.TABLE_NAME + " (" +
                PhotoEntry._ID + " INTEGER PRIMARY KEY," +
                PhotoEntry.COLUMN_KEY + " TEXT UNIQUE ON CONFLICT REPLACE," +
                PhotoEntry.COLUMN_CREATED_AT + " INTEGER NOT NULL, " +
                PhotoEntry.COLUMN_URL + " REAL NOT NULL, " +
                PhotoEntry.COLUMN_USER_ID + " REAL NOT NULL, " +
                PhotoEntry.COLUMN_LOCATION_KEY + " TEXT NOT NULL " +
                " );";
        sqLiteDatabase.execSQL(SQL_CREATE_PHOTO_TABLE);

        final String SQL_CREATE_RATE_TABLE = "CREATE TABLE " + RateEntry.TABLE_NAME + " (" +
                RateEntry._ID + " INTEGER PRIMARY KEY," +
                RateEntry.COLUMN_KEY + " TEXT UNIQUE ON CONFLICT REPLACE, " +
                RateEntry.COLUMN_CREATED_AT + " INTEGER NOT NULL, " +
                RateEntry.COLUMN_RATE + " REAL NOT NULL, " +
                RateEntry.COLUMN_USER_ID + " REAL NOT NULL, " +
                RateEntry.COLUMN_USER_NAME + " TEXT NOT NULL, " +
                RateEntry.COLUMN_USER_PICTURE_URL + " TEXT NOT NULL " +
                " );";
        sqLiteDatabase.execSQL(SQL_CREATE_RATE_TABLE);
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
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + CommentEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PhotoEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + RateEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
