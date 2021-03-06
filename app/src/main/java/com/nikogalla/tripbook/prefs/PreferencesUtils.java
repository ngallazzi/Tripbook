package com.nikogalla.tripbook.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.Log;

import com.nikogalla.tripbook.R;

/**
 * Created by Nicola on 2016-11-22.
 */

public class PreferencesUtils {
    private static final String TAG = PreferencesUtils.class.getSimpleName();
    private Context mContext;
    SharedPreferences mPreferences;


    public PreferencesUtils(Context context) {
        mContext = context;
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public int getPreferredUserRange(){
        String defDistance = mContext.getString(R.string.def_distance);
        int defDistanceInt = Integer.valueOf(defDistance);
        try{
            String prefDistance = mPreferences.getString(mContext.getString(R.string.preference_distance_range_key),"");
            int intValueDistance = Integer.valueOf(prefDistance);
            return intValueDistance;
        }catch (Exception e ){

        }
        return defDistanceInt;
    }

    public String getPreferredDistanceUnit(){
        String prefDistance = mPreferences.getString(mContext.getString(R.string.preference_distance_unit_key),"km");
        return prefDistance;
    }
}
