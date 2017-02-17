package com.nikogalla.tripbook;

import android.*;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
import com.nikogalla.tripbook.data.LocationDbHelper;
import com.nikogalla.tripbook.sync.TripbookSyncAdapter;
import com.nikogalla.tripbook.utils.ImageUtils;
import com.nikogalla.tripbook.utils.LocationUtils;
import com.nikogalla.tripbook.utils.NetworkUtils;
import com.nikogalla.tripbook.utils.StatusSnackBars;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

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

public class AroundYouMapActivity extends AppCompatActivity implements GoogleMap.OnInfoWindowClickListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    final String TAG = AroundYouMapActivity.class.getSimpleName();
    final int MAP_ZOOM = 10;
    Map<Marker, String> markers;
    Context mContext;
    LatLng marker;
    private GoogleMap mMap;
    @BindView(R.id.clMapContainer)
    CoordinatorLayout clMapContainer;
    @BindView(R.id.tbAroundYouMap)
    Toolbar tbAroundYouMap;
    @BindView(R.id.llMapSelection)
    LinearLayout llMapSelection;
    ArrayList<com.nikogalla.tripbook.models.Location> mLocationsArrayList;
    FirebaseDatabase mDatabase;
    GoogleApiClient mGoogleApiClient;
    Location currentLocation;

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
        mMap.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
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


    public void getLocations(final Location curLocation) {
        mLocationsArrayList.clear();
        DatabaseReference ref = mDatabase.getReference(com.nikogalla.tripbook.models.Location.LOCATION_TABLE_NAME);
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                com.nikogalla.tripbook.models.Location location = dataSnapshot.getValue(com.nikogalla.tripbook.models.Location.class);
                int distance = LocationUtils.getLocationDistanceFromMyLocation(location, curLocation);
                location.distance = distance;
                if (LocationUtils.isLocationDistanceInRange(distance, mContext)) {
                    mLocationsArrayList.add(location);
                    location.setKey(dataSnapshot.getKey());
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.v(TAG, "Child changed");
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.v(TAG, "Child removed");
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.v(TAG, "Child moved");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Done with the initial loading
                if (mLocationsArrayList.size() == 0) {

                } else {
                    Collections.sort(mLocationsArrayList, new LocationUtils.LocationDistanceComparator());
                    // Saving location locally for widget
                    LocationDbHelper.saveLocationsLocally(mLocationsArrayList, mContext);
                    TripbookSyncAdapter.updateWidgets(mContext);
                    int locIndex = 0;
                    for (com.nikogalla.tripbook.models.Location l : mLocationsArrayList) {
                        marker = new LatLng(l.latitude, l.longitude);
                        Marker m = mMap.addMarker(new MarkerOptions()
                                .title(l.name)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker))
                                .position(marker));
                        m.setSnippet(l.key);
                        markers.put(m, l.key);
                        locIndex++;
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
        String locationName = marker.getTitle();
        Intent intent = new Intent(mContext, LocationDetailsActivity.class);
        intent.putExtra(getString(R.string.location_key_id), locationId);
        intent.putExtra(getString(R.string.location_name_id), locationName);
        startActivity(intent);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if (currentLocation!=null){
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), MAP_ZOOM));
                    getLocations(currentLocation);
                }else{
                    StatusSnackBars.getErrorSnackBar(getString(R.string.database_error),clMapContainer,AroundYouMapActivity.this).show();
                }
                return;
            }
        }catch (Exception e){
            Log.d(TAG,"Error getting location: " + e.getMessage());
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        StatusSnackBars.getErrorSnackBar(getString(R.string.database_error),clMapContainer,AroundYouMapActivity.this).show();
    }

    private class TripBookInfoWindow implements GoogleMap.InfoWindowAdapter {
        private View infoWindow;
        private final ImageView ivLocation;
        private TextView tvLocationName,tvLocationAddress;
        com.nikogalla.tripbook.models.Location currentLocation;

        public TripBookInfoWindow() {
            infoWindow = getLayoutInflater().inflate(R.layout.item_location_info_window,null);
            ivLocation = (ImageView) infoWindow.findViewById(R.id.ivLocation);
            tvLocationName = (TextView) infoWindow.findViewById(R.id.tvLocationName);
            tvLocationAddress = (TextView) infoWindow.findViewById(R.id.tvLocationAddress);
        }

        @Override
        public View getInfoWindow(final Marker marker) {
            currentLocation = new com.nikogalla.tripbook.models.Location();
            for (com.nikogalla.tripbook.models.Location l : mLocationsArrayList) {
                if (marker.getTitle().matches(l.name)) {
                    currentLocation = l;
                    break;
                }
            }
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            if (currentLocation!=null){
                tvLocationName.setText(currentLocation.name);
                tvLocationAddress.setText(currentLocation.address);
                Bitmap bitmap = new ImageUtils(currentLocation,mContext).getLocalBitmapForLocation();
                ivLocation.setImageBitmap(bitmap);
            }
            return infoWindow;
        }
    }

}
