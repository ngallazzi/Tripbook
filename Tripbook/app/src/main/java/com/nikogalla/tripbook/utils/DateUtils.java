package com.nikogalla.tripbook.utils;

import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Nicola on 2017-01-29.
 */

public class DateUtils {
    final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm",Locale.getDefault());

    public static String getHumanReadableDateString(String date){
        final String TAG = DateUtils.class.getSimpleName();
        String formattedDate = null;
        try{
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date d = sdf.parse(date);
            DateFormat f = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault());
            formattedDate = f.format(d);
        } catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return formattedDate;
    }

    public static String getUTCDateStringFromdate(Date date){
        final String TAG = DateUtils.class.getSimpleName();
        String formattedDate = null;
        try{
            formattedDate = sdf.format(date);
        } catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return formattedDate;
    }
}
