package com.nikogalla.tripbook;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.nikogalla.tripbook.adding.AddLocationActivity;
import com.nikogalla.tripbook.adding.AddTagActivity;
import com.nikogalla.tripbook.data.FirebaseHelper;
import com.nikogalla.tripbook.models.Location;
import com.nikogalla.tripbook.prefs.SettingsActivity;
import com.nikogalla.tripbook.sync.TripbookSyncAdapter;
import com.nikogalla.tripbook.utils.ImageUtils;
import com.nikogalla.tripbook.utils.LocationUtils;
import com.nikogalla.tripbook.utils.StatusSnackBars;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AroundYouActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,ResultCallback<LocationSettingsResult> {
    private static final String TAG = AroundYouActivity.class.getSimpleName();
    final int SETTINGS_REQUEST_CODE = 1;
    @BindView(R.id.clActivityAroundYouContainer)
    CoordinatorLayout clActivityAroundYouContainer;
    @BindView(R.id.tbAroundYou)
    Toolbar tbAroundYou;
    @BindView(R.id.rvLocations)
    RecyclerView mRvLocations;
    @BindView(R.id.tvNoLocationsFound)
    TextView tvNoLocationsFound;
    @BindView(R.id.fabAddLocation)
    FloatingActionButton fabAddLocation;
    private LocationAdapter mLocationsAdapter;
    private ArrayList<Location> mLocationsArrayList;
    private LinearLayoutManager mLayoutManager;
    FirebaseDatabase mDatabase;
    private Context mContext;
    private final int LOCATION_REQUEST_ID = 1;
    private final String SAVED_RECYCLER_VIEW_STATUS_ID = "rv_status_id";

    GoogleApiClient mGoogleApiClient;
    Parcelable listState;
    Account mSyncAccount;
    ContentResolver mResolver;
    FusedLocationProviderClient mFusedLocationClient;
    LocationRequest mLocationRequest;
    LocationCallback mLocationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_around_you);
        mContext = this;
        ButterKnife.bind(this);
        tbAroundYou.setTitle(getString(R.string.app_name) + " - " +getString(R.string.around_you));
        setSupportActionBar(tbAroundYou);
        mDatabase = FirebaseHelper.getDatabase();

        FirebaseMessaging.getInstance().subscribeToTopic("report");

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        fabAddLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext,AddLocationActivity.class);
                startActivity(intent);
            }
        });

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        initGoogleApiClient();
        initLocationRequest();
        initLocationRetrievedCallback();


        mSyncAccount = CreateSyncAccount(mContext);
        mResolver = getContentResolver();
        TripbookSyncAdapter.configurePeriodicSync(mSyncAccount,mContext); // Sync every 3 hours with 20 minutes of flex
        initRecyclerView();
    }

    private void initGoogleApiClient(){
        // Create an instance of GoogleAPIClient.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        mGoogleApiClient.connect();
    }

    private void initLocationRequest(){
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(30 * 1000);
        mLocationRequest.setFastestInterval(5 * 1000);
    }

    private void initLocationRetrievedCallback(){
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (android.location.Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    // ...
                    getLocations(location);
                }
                mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            };
        };
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates(){
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                null /* Looper */);
    }

    private void initRecyclerView(){
        mRvLocations.setHasFixedSize(true);
        mLocationsArrayList = new ArrayList<>();
        // use a linear layout manager

        mLayoutManager = new LinearLayoutManager(this);
        mRvLocations.setLayoutManager(mLayoutManager);
        // specify an adapter (see also next example)

        mLocationsAdapter = new LocationAdapter(mLocationsArrayList,mContext);
        mRvLocations.setAdapter(mLocationsAdapter);

        mRvLocations.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (isLastItemDisplaying())
                {
                    fabAddLocation.hide();
                }else{
                    if (fabAddLocation.getVisibility() == View.GONE || fabAddLocation.getVisibility() == View.INVISIBLE){
                        fabAddLocation.show();
                    }
                }
            }
        });
    }

    private boolean isLastItemDisplaying() {
        if (mRvLocations.getAdapter().getItemCount() != 0) {
            int lastVisibleItemPosition = ((LinearLayoutManager) mRvLocations.getLayoutManager()).findLastCompletelyVisibleItemPosition();
            if (lastVisibleItemPosition != RecyclerView.NO_POSITION && lastVisibleItemPosition == mRvLocations.getAdapter().getItemCount() - 1)
                return true;
        }
        return false;
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (!mGoogleApiClient.isConnected()){
            mGoogleApiClient.connect();
        }
    }

    public void getLocations(final android.location.Location gpsLocation){
        Log.v(TAG,"getting locations");
        mLocationsArrayList.clear();
        DatabaseReference ref = mDatabase.getReference(Location.LOCATION_TABLE_NAME);
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Location location = dataSnapshot.getValue(Location.class);
                int distance = LocationUtils.getLocationDistanceFromMyLocation(location,gpsLocation);
                location.distance = distance;
                if (LocationUtils.isLocationDistanceInRange(distance,mContext)){
                    mLocationsArrayList.add(location);
                    new ImageUtils(location,mContext).saveImageLocally();
                    location.setKey(dataSnapshot.getKey());
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.v(TAG,"Child changed");
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.v(TAG,"Child removed");
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.v(TAG,"Child moved");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                showLocationErrorSnackbar();
            }
        });
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Done with the initial loading
               if (mLocationsArrayList.size() ==0){
                   tvNoLocationsFound.setVisibility(View.VISIBLE);
                   mRvLocations.setVisibility(View.GONE);
               }else{
                   Collections.sort(mLocationsArrayList,new LocationUtils.LocationDistanceComparator());
                   // Saving location locally for widget
                   TripbookSyncAdapter.syncImmediately(mContext,mSyncAccount);
                   mLocationsAdapter.notifyDataSetChanged();
                   tvNoLocationsFound.setVisibility(View.GONE);
                   mRvLocations.setVisibility(View.VISIBLE);
                   if (listState!=null){
                       mRvLocations.getLayoutManager().onRestoreInstanceState(listState);
                       Log.v(TAG,"Restoring recycler view state");
                   }
               }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        // Get the SearchView and set the searchable configuration
        MenuItem itemSignOut = menu.findItem(R.id.action_logout);
        itemSignOut.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                SignOutUser();
                return false;
            }
        });
        MenuItem itemDistance = menu.findItem(R.id.action_preferences);
        itemDistance.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(mContext,SettingsActivity.class);
                startActivity(intent);
                return false;
            }
        });
        MenuItem itemMapLayout = menu.findItem(R.id.action_map_layout);
        itemMapLayout.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(mContext,AroundYouMapActivity.class);
                startActivity(intent);
                return false;
            }
        });
        MenuItem itemAccounts = menu.findItem(R.id.action_user_account);
        itemAccounts.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(mContext,UserAccountInfoActivity.class);
                startActivity(intent);
                return false;
            }
        });
        MenuItem itemTags = menu.findItem(R.id.action_add_tag);
        itemTags.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(mContext,AddTagActivity.class);
                startActivity(intent);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private void SignOutUser(){
        FirebaseAuth.getInstance().signOut();
        finish();
        Intent intent = new Intent(mContext, SignUpActivity.class);
        startActivity(intent);
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        checkLocationSettings();
    }

    private void checkLocationSettings(){
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        mGoogleApiClient,
                        builder.build()
                );

        result.setResultCallback(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case LOCATION_REQUEST_ID:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (!mGoogleApiClient.isConnected()){
                        mGoogleApiClient.connect();
                    }
                }else{
                    showLocationErrorSnackbar();
                }
                return;
            }
        }
    }

    private void showLocationErrorSnackbar(){
        final Snackbar snackbar = StatusSnackBars.getErrorSnackBar(getString(R.string.alert_localization),clActivityAroundYouContainer);
        snackbar.setAction(getString(R.string.settings), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLocationSettings();
                snackbar.dismiss();
            }
        }).show();
    }


    @Override
    public void onConnectionSuspended(int i) {
        Log.v(TAG,"Connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        showLocationErrorSnackbar();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Parcelable listState = mRvLocations.getLayoutManager().onSaveInstanceState();
        outState.putParcelable(SAVED_RECYCLER_VIEW_STATUS_ID, listState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            listState = savedInstanceState.getParcelable(SAVED_RECYCLER_VIEW_STATUS_ID);
        }
    }

    public static Account CreateSyncAccount(Context context) {
        // Create the account type and default account
        Account newAccount = new Account(context.getString(R.string.app_name), context.getString(R.string.sync_account_type));
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(
                        ACCOUNT_SERVICE);
        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
        if ( null == accountManager.getPassword(newAccount) ) {
            if (accountManager.addAccountExplicitly(newAccount, null, null)) {
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call context.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
            } else {
                Log.d(TAG,"Account already exists");
            /*
             * The account exists or some other error occurred. Log this, report it,
             * or handle it internally.
             * Log.
             */
            }
        }
        return newAccount;
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                // NO need to show the dialog;
                mFusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                    if (location!=null){
                        getLocations(location);

                    }else{
                        startLocationUpdates();
                    }
                });
                break;

            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                //  Location settings are not satisfied. Show the user a dialog

                try {
                    // Show the dialog by calling startResolutionForResult(), and check the result
                    // in onActivityResult().

                    status.startResolutionForResult(AroundYouActivity.this, LOCATION_REQUEST_ID);

                } catch (IntentSender.SendIntentException e) {

                    //failed to show
                }
                break;

            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                // Location settings are unavailable so not possible to show any dialog now
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOCATION_REQUEST_ID) {
            if (resultCode == RESULT_OK) {
                startLocationUpdates();
            } else {
                initRecyclerView();
                showLocationErrorSnackbar();
            }

        }
    }
}
