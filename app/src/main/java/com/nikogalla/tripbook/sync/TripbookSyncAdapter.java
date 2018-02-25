package com.nikogalla.tripbook.sync;


import android.accounts.Account;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nikogalla.tripbook.AroundYouActivity;
import com.nikogalla.tripbook.R;
import com.nikogalla.tripbook.data.FirebaseHelper;
import com.nikogalla.tripbook.data.LocationContract;
import com.nikogalla.tripbook.data.LocationDbHelper;
import com.nikogalla.tripbook.models.Location;
import com.nikogalla.tripbook.utils.LocationUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

/**
 * Created by Nicola on 2017-02-10.
 */

public class TripbookSyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = TripbookSyncAdapter.class.getSimpleName();
    public static final String ACTION_DATA_UPDATED = "com.nikogalla.tripbook.ACTION_DATA_UPDATED";
    FirebaseDatabase mDatabase;
    private static Context mContext;
    ArrayList<Location> mLocationArrayList = new ArrayList<>();
    private static final int PERIODIC_SYNC_INTERVAL = 60000;
    private static final int FLEX_TIME = 60000;

    GpsLocationHelper mGpsLocationHelper;
    final int NEW_LOCATIONS_NOTIFICATION_ID = 1;
    ContentResolver mContentResolver;
    /**
     * Set up the sync adapter
     */
    public TripbookSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        mContext = context;
        mDatabase = FirebaseHelper.getDatabase();
        mGpsLocationHelper = new GpsLocationHelper(mContext);
        mContentResolver = context.getContentResolver();
    }

    public TripbookSyncAdapter(
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
        mDatabase = FirebaseHelper.getDatabase();
        mGpsLocationHelper = new GpsLocationHelper(mContext);
        mContentResolver = context.getContentResolver();
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context, Account account) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(account, context.getString(R.string.content_authority), bundle);
    }

    @Override
    public void onPerformSync(
            Account account,
            Bundle extras,
            String authority,
            ContentProviderClient provider,
            SyncResult syncResult) {

        Date date = new Date();
        Log.v(TAG,"performing sync: " + date.toLocaleString());
        fetchDataFromFirebase();
    }

    public void fetchDataFromFirebase(){
        mLocationArrayList = new ArrayList<>();
        DatabaseReference ref = mDatabase.getReference(Location.LOCATION_TABLE_NAME);
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Location location = dataSnapshot.getValue(Location.class);
                android.location.Location currentLocation = mGpsLocationHelper.getmGpsLocation();
                int distance = LocationUtils.getLocationDistanceFromMyLocation(location,currentLocation);
                location.distance = distance;
                if (LocationUtils.isLocationDistanceInRange(distance,mContext)){
                    mLocationArrayList.add(location);
                    location.setKey(dataSnapshot.getKey());
                }
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
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Done with the initial loading
                if (mLocationArrayList.size() > 0){
                    Collections.sort(mLocationArrayList,new LocationUtils.LocationDistanceComparator());
                    int newLocationCount = getNewLocationsCount();
                    // Saving location locally for widget
                    LocationDbHelper.saveLocationsLocally(mLocationArrayList,mContext);
                    updateWidgets(mContext);
                    // Generate notification if data has changed
                    if (newLocationCount>0){
                        Log.v(TAG,"new location count: " + newLocationCount);
                        generateNotification(newLocationCount);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public int getNewLocationsCount(){
        int count = 0;
        try{
            Cursor cursorSavedLocations = mContext.getContentResolver().query(LocationContract.LocationEntry.CONTENT_URI,null,null,null,null);
            while (cursorSavedLocations.moveToNext()) {
                boolean found = false;
                String key = cursorSavedLocations.getString(cursorSavedLocations.getColumnIndex(LocationContract.LocationEntry.COLUMN_KEY));
                for (Location l : mLocationArrayList){
                    if (l.key.matches(key)){
                        found = true;
                    }
                }
                if (!found){
                    count++;
                }
            }

        }catch (Exception e){
            Log.v(TAG,"can't calculate new location count: "+ e.getMessage());
        }
        return count;
    }

    public static void updateWidgets(Context context) {
        Intent dataUpdatedIntent = new Intent();
        dataUpdatedIntent.setAction(ACTION_DATA_UPDATED);
        context.sendBroadcast(dataUpdatedIntent);
    }

    public void generateNotification(int locationCount){
        if (locationCount>0){
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(mContext)
                            .setSmallIcon(R.drawable.ic_tripbook_notification)
                            .setAutoCancel(true)
                            .setContentTitle(mContext.getString(R.string.new_locations_found))
                            .setContentText(mContext.getString(R.string.touch_to_see));
// Creates an explicit intent for an Activity in your app
            Intent resultIntent = new Intent(mContext, AroundYouActivity.class);

// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
// Adds the back stack for the Intent (but not the Intent itself)
            stackBuilder.addParentStack(AroundYouActivity.class);
// Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
            mNotificationManager.notify(NEW_LOCATIONS_NOTIFICATION_ID, mBuilder.build());
        }
    }

    public static void configurePeriodicSync(Account account, Context context) {
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(PERIODIC_SYNC_INTERVAL, FLEX_TIME).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), PERIODIC_SYNC_INTERVAL);
        }
    }

}
