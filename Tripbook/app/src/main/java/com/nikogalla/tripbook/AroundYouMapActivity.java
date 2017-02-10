package com.nikogalla.tripbook;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nikogalla.tripbook.data.FirebaseHelper;
import com.nikogalla.tripbook.data.LocationContract;
import com.nikogalla.tripbook.sync.TripbookSyncAdapter;
import com.nikogalla.tripbook.utils.LocationUtils;
import com.nikogalla.tripbook.utils.NetworkUtils;
import com.nikogalla.tripbook.utils.StatusSnackBars;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Nicola on 2017-02-10.
 */

public class AroundYouMapActivity extends AppCompatActivity implements GoogleMap.OnInfoWindowClickListener, OnMapReadyCallback,  GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    final String TAG = AroundYouMapActivity.class.getSimpleName();
    final int MAP_ZOOM = 10;
    Map<Marker, String> markers;
    Context mContext;
    LatLng marker;
    private GoogleMap mMap;
    @BindView(R.id.tbAroundYouMap)
    Toolbar tbAroundYouMap;
    @BindView(R.id.llMapSelection)
    LinearLayout llMapSelection;
    ArrayList<com.nikogalla.tripbook.models.Location> mLocationsArrayList;
    FirebaseDatabase mDatabase;
    GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_around_you_map);
        ButterKnife.bind(this);
        tbAroundYouMap = (Toolbar) findViewById(R.id.tbAroundYouMap);
        tbAroundYouMap.setTitle(getString(R.string.app_name) + " - " + getString(R.string.around_you));
        setSupportActionBar(tbAroundYouMap);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        llMapSelection = (LinearLayout) findViewById(R.id.llMapSelection);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mLocationsArrayList = new ArrayList<>();
        markers = new HashMap<Marker, String>();
        mDatabase = FirebaseHelper.getDatabase();
        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
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
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnInfoWindowClickListener(this);
        mMap.setInfoWindowAdapter(new TripBookInfoWindow());
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            mMap.setMyLocationEnabled(true);
            return;
        }
    }


    public void getLocations(final Location curLocation){
        mLocationsArrayList.clear();
        DatabaseReference ref = mDatabase.getReference(com.nikogalla.tripbook.models.Location.LOCATION_TABLE_NAME);
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                com.nikogalla.tripbook.models.Location location = dataSnapshot.getValue(com.nikogalla.tripbook.models.Location.class);
                int distance = LocationUtils.getLocationDistanceFromMyLocation(location,curLocation);
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

            }
        });
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Done with the initial loading
                if (mLocationsArrayList.size() ==0){

                }else{
                    Collections.sort(mLocationsArrayList,new LocationUtils.LocationDistanceComparator());
                    // Saving location locally for widget
                    LocationContract.LocationEntry.saveLocationsLocally(mLocationsArrayList,mContext);
                    TripbookSyncAdapter.updateWidgets(mContext);
                    int locIndex = 0;
                    for (com.nikogalla.tripbook.models.Location l: mLocationsArrayList){
                        if (locIndex == 0){
                            LatLng coordinates = new LatLng(l.latitude,l.longitude);
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, MAP_ZOOM));
                        }
                        marker = new LatLng(l.latitude, l.longitude);
                        Marker m = mMap.addMarker(new MarkerOptions()
                                .title(l.name)
                                .position(marker));
                        m.setSnippet(l.key);
                        markers.put(m, l.key);
                        locIndex ++;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        String locationId = marker.getSnippet();
        Intent intent = new Intent(mContext,LocationDetailsActivity.class);
        intent.putExtra(getString(R.string.location_id),locationId);
        startActivity(intent);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        try{
            Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            getLocations(currentLocation);
        }catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private class TripBookInfoWindow implements GoogleMap.InfoWindowAdapter {
        private final View infoWindow;
        private final ImageView ivLocation;
        private final TextView tvLocationName,tvLocationAddress;
        public TripBookInfoWindow() {
            infoWindow = getLayoutInflater().inflate(R.layout.item_location_info_window,null);
            ivLocation = (ImageView) infoWindow.findViewById(R.id.ivLocation);
            tvLocationName = (TextView) infoWindow.findViewById(R.id.tvLocationName);
            tvLocationAddress = (TextView) infoWindow.findViewById(R.id.tvLocationAddress);
        }

        @Override
        public View getInfoWindow(final Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            for (com.nikogalla.tripbook.models.Location l : mLocationsArrayList){
                if (marker.getTitle().matches(l.name)){
                    tvLocationName.setText(l.name);
                    tvLocationAddress.setText(l.address);
                    if (NetworkUtils.isOnline(mContext)){
                        Picasso.with(mContext).load(l.getMainPhotoUrl()).into(ivLocation);
                    }else{
                        Picasso.with(mContext).load(l.getMainPhotoUrl()).networkPolicy(NetworkPolicy.OFFLINE).into(ivLocation);
                        Log.v(TAG,"no connection, loading local images");
                    }
                }
            }
            return infoWindow;
        }
    }
}
