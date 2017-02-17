package com.nikogalla.tripbook.utils;

/**
 * Created by Nicola on 2017-02-09.
 */

public class DistanceUtils {
    public static String getDistanceInKm(int meters){
        double distanceInKm = meters*0.001;
        String distance = String.format("%.1f", distanceInKm) + " km";
        return distance;
    }

    public static String getDistanceInMiles(int meters){
        double distanceInMiles = meters*0.000621371192;
        String distance = String.format("%.1f", distanceInMiles) + " miles";
        return distance;
    }
}
