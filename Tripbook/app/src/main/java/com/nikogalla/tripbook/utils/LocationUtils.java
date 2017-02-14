package com.nikogalla.tripbook.utils;

import android.content.Context;

import com.nikogalla.tripbook.R;
import com.nikogalla.tripbook.models.Location;
import com.nikogalla.tripbook.prefs.PreferencesUtils;

import java.util.Comparator;

/**
 * Created by Nicola on 2017-02-10.
 */

public class LocationUtils {
    public static boolean isLocationDistanceInRange(double distanceInMeters, Context context){
        final float KM_TO_METER_MULTIPLIER = 1000.0f;
        final float MI_TO_METER_MULTIPLIER = 1609.34f;
        PreferencesUtils prefs = new PreferencesUtils(context);
        String unit = prefs.getPreferredDistanceUnit();
        float preferredDistance;
        if (unit.matches(context.getString(R.string.kilometers))){
            preferredDistance = new PreferencesUtils(context).getPreferredUserRange()*KM_TO_METER_MULTIPLIER;
        }else{
            preferredDistance = new PreferencesUtils(context).getPreferredUserRange()*MI_TO_METER_MULTIPLIER;
        }
        if (distanceInMeters<= preferredDistance){
            return true;
        }
        return false;
    }
    public static int getLocationDistanceFromMyLocation(Location testLocation, android.location.Location gpsLocation){
        if (gpsLocation !=null){
            android.location.Location targetLocation = new android.location.Location("");//provider name is unecessary
            targetLocation.setLatitude(testLocation.latitude);//your coords of course
            targetLocation.setLongitude(testLocation.longitude);
            return (int) gpsLocation.distanceTo(targetLocation);
        }else{
            return (int) Integer.MAX_VALUE;
        }
    }

    public static class LocationDistanceComparator implements Comparator<Location>
    {
        public int compare(Location loc1, Location loc2) {
            return (int) (loc1.distance-loc2.distance);
        }
    }
}
