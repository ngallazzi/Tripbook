package com.nikogalla.tripbook;

import android.Manifest;
import android.accounts.Account;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
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
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nikogalla.tripbook.data.FirebaseHelper;
import com.nikogalla.tripbook.data.LocationContract;
import com.nikogalla.tripbook.models.Location;
import com.nikogalla.tripbook.prefs.PreferencesUtils;
import com.nikogalla.tripbook.prefs.SettingsActivity;
import com.nikogalla.tripbook.sync.TripbookSyncAdapter;
import com.nikogalla.tripbook.utils.LocationUtils;
import com.nikogalla.tripbook.utils.StatusSnackBars;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AroundYouActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private final String TAG = AroundYouActivity.class.getSimpleName();
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

    GoogleApiClient mGoogleApiClient;
    android.location.Location mGpsLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_around_you);
        mContext = this;
        ButterKnife.bind(this);
        tbAroundYou.setTitle(getString(R.string.app_name) + " - " +getString(R.string.around_you));
        setSupportActionBar(tbAroundYou);
        mDatabase = FirebaseHelper.getDatabase();
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRvLocations.setHasFixedSize(true);
        mLocationsArrayList = new ArrayList<>();
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRvLocations.setLayoutManager(mLayoutManager);
        // specify an adapter (see also next example)
        mLocationsAdapter = new LocationAdapter(mLocationsArrayList,mContext);
        mRvLocations.setAdapter(mLocationsAdapter);
        fabAddLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext,AddLocationActivity.class);
                startActivity(intent);
            }
        });
        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mRvLocations.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 ||dy<0 && fabAddLocation.isShown())
                {
                    fabAddLocation.hide();
                }
            }
            public void onScrollStateChanged(RecyclerView recyclerView, int newState)
            {
                if (newState == RecyclerView.SCROLL_STATE_IDLE && !isLastItemDisplaying())
                {
                    fabAddLocation.show();
                }

                super.onScrollStateChanged(recyclerView, newState);
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
    protected void onStart() {
        // Connect the client.
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    public void getLocations(){
        mLocationsArrayList.clear();
        DatabaseReference ref = mDatabase.getReference(Location.LOCATION_TABLE_NAME);
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Location location = dataSnapshot.getValue(Location.class);
                int distance = LocationUtils.getLocationDistanceFromMyLocation(location,mGpsLocation);
                location.distance = distance;
                if (LocationUtils.isLocationDistanceInRange(distance,mContext)){
                    mLocationsArrayList.add(location);
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
                StatusSnackBars.getErrorSnackBar(getString(R.string.database_error),clActivityAroundYouContainer).show();
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
                   LocationContract.LocationEntry.saveLocationsLocally(mLocationsArrayList,mContext);
                   TripbookSyncAdapter.updateWidgets(mContext);
                   mLocationsAdapter.notifyDataSetChanged();
                   tvNoLocationsFound.setVisibility(View.GONE);
                   mRvLocations.setVisibility(View.VISIBLE);
               }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
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
        return super.onCreateOptionsMenu(menu);
    }

    private void SignOutUser(){
        FirebaseAuth.getInstance().signOut();
        finish();
        Intent intent = new Intent(mContext, SignUpActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mGpsLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            TripbookSyncAdapter.initializeSyncAdapter(mContext);
            getLocations();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST_ID);
        }
        // Test, remove
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case LOCATION_REQUEST_ID:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    mGpsLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    getLocations();
                }else{
                    Toast.makeText(mContext,getString(R.string.alert_localization),Toast.LENGTH_SHORT).show();
                    getLocations();
                }
                return;
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


}
