package com.nikogalla.tripbook.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Nicola on 2017-02-03.
 */

public class LocationProvider extends ContentProvider {
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private static final String TAG = LocationProvider.class.getSimpleName();
    private LocationDbHelper mOpenHelper;

    static final int LOCATION = 100;
    static final int LOCATION_WITH_LOCATION_KEY = 101;
    static final int LOCATION_WITH_COORDINATES = 102;
    static final int COMMENT = 104;
    static final int COMMENT_WITH_LOCATION_KEY = 105;
    static final int PHOTO = 106;
    static final int PHOTO_WITH_LOCATION_KEY = 107;
    static final int RATE = 108;
    static final int RATE_WITH_LOCATION_KEY = 109;

    @Override
    public boolean onCreate() {
        mOpenHelper = new LocationDbHelper(getContext());
        return true;
    }

    static UriMatcher buildUriMatcher() {
        // I know what you're thinking.  Why create a UriMatcher when you can use regular
        // expressions instead?  Because you're not crazy, that's why.

        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = LocationContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, LocationContract.PATH_LOCATION, LOCATION);
        matcher.addURI(authority, LocationContract.PATH_LOCATION + "/*", LOCATION_WITH_LOCATION_KEY);
        matcher.addURI(authority, LocationContract.PATH_LOCATION + "/*/*", LOCATION_WITH_COORDINATES);

        matcher.addURI(authority, LocationContract.PATH_COMMENT, COMMENT);
        matcher.addURI(authority, LocationContract.PATH_COMMENT + "/*", COMMENT_WITH_LOCATION_KEY);
        matcher.addURI(authority, LocationContract.PATH_PHOTO, PHOTO);
        matcher.addURI(authority, LocationContract.PATH_PHOTO + "/*", PHOTO_WITH_LOCATION_KEY);
        matcher.addURI(authority, LocationContract.PATH_RATE, RATE);
        matcher.addURI(authority, LocationContract.PATH_RATE + "/*", RATE_WITH_LOCATION_KEY);

        return matcher;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            // Student: Uncomment and fill out these two cases
            case LOCATION:
                return LocationContract.LocationEntry.CONTENT_TYPE;
            case LOCATION_WITH_LOCATION_KEY:
                return LocationContract.LocationEntry.CONTENT_ITEM_TYPE;
            case LOCATION_WITH_COORDINATES:
                return LocationContract.LocationEntry.CONTENT_TYPE;
            case COMMENT_WITH_LOCATION_KEY:
                return LocationContract.CommentEntry.CONTENT_TYPE;
            case PHOTO_WITH_LOCATION_KEY:
                return LocationContract.PhotoEntry.CONTENT_TYPE;
            case RATE_WITH_LOCATION_KEY:
                return LocationContract.RateEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case LOCATION: {
                long _id = db.insert(LocationContract.LocationEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = LocationContract.LocationEntry.buildLocationUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case COMMENT: {
                long _id = db.insert(LocationContract.CommentEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = LocationContract.CommentEntry.buildCommentUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case PHOTO: {
                long _id = db.insert(LocationContract.PhotoEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = LocationContract.LocationEntry.buildLocationUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case RATE: {
                long _id = db.insert(LocationContract.RateEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = LocationContract.LocationEntry.buildLocationUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case LOCATION:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _idLocation = db.insert(LocationContract.LocationEntry.TABLE_NAME, null, value);
                        long _idComments = db.insert(LocationContract.CommentEntry.TABLE_NAME, null, value);
                        long _idPhotos = db.insert(LocationContract.PhotoEntry.TABLE_NAME, null, value);
                        long _idRates = db.insert(LocationContract.RateEntry.TABLE_NAME, null, value);
                        if (_idLocation != -1 && _idComments != -1 && _idPhotos !=-1 && _idRates != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
