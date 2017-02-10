package com.nikogalla.tripbook.utils;

import android.content.Context;

import com.nikogalla.tripbook.models.Location;
import com.nikogalla.tripbook.prefs.PreferencesUtils;

import java.util.Comparator;

/**
 * Created by Nicola on 2017-02-10.
 */

public class LocationUtils {
    public static boolean isLocationDistanceInRange(double distance, Context context){
        int preferredDistance = new PreferencesUtils(context).getPreferredUserRange()*1000;
        if (distance<= preferredDistance){
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
