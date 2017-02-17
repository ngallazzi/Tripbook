package com.nikogalla.tripbook.sync;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.nikogalla.tripbook.models.Comment;

/**
 * Created by Nicola on 2017-02-10.
 */

public class GpsLocationHelper implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener  {
    private static final String TAG = GpsLocationHelper.class.getSimpleName();
    GoogleApiClient mGoogleApiClient;
    private Context mContext;
    private android.location.Location mGpsLocation;

    public GpsLocationHelper(Context context) {
        mContext = context;
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();
        }
    }

    public Location getmGpsLocation() {
        return mGpsLocation;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        try{
            mGpsLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }catch (Exception e){
            Log.d(TAG,"No location permission: " +e.getMessage());
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG,"unable to connect to Location Services");
    }
}
