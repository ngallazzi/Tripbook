package com.nikogalla.tripbook.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nikogalla.tripbook.data.LocationContract;
import com.nikogalla.tripbook.models.Location;

/**
 * Created by Nicola on 2017-02-03.
 */

public class SyncAdapter extends AbstractThreadedSyncAdapter {
    // Global variables
    // Define a variable to contain a content resolver instance
    ContentResolver mContentResolver;
    Context mContext;
    /**
     * Set up the sync adapter
     */
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        mContentResolver = context.getContentResolver();
    }
    /**
     * Set up the sync adapter. This form of the
     * constructor maintains compatibility with Android 3.0
     * and later platform versions
     */
    public SyncAdapter(
            Context context,
            boolean autoInitialize,
            boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        mContentResolver = context.getContentResolver();
        mContext = context;
    }

    @Override
    public void onPerformSync(
            Account account,
            Bundle extras,
            String authority,
            ContentProviderClient provider,
            SyncResult syncResult) {

            getLocationsByProximity (null,null);
    }

    public void getLocationsByProximity(Double longitude, Double latitude){
        FirebaseDatabase mDatabase;
        mDatabase = FirebaseDatabase.getInstance();
        DatabaseReference ref = mDatabase.getReference(Location.LOCATION_TABLE_NAME);
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Location location = dataSnapshot.getValue(Location.class);
                location.setKey(dataSnapshot.getKey());
                LocationContract.LocationEntry.saveLocationLocally(location,mContext);
                LocationContract.CommentEntry.saveLocationCommentsLocally(location,mContext);
                LocationContract.PhotoEntry.saveLocationPhotosLocally(location,mContext);
                LocationContract.RateEntry.saveLocationRatesLocally(location,mContext);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
