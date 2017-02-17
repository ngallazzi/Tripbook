package com.nikogalla.tripbook.widget;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Binder;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.nikogalla.tripbook.R;
import com.nikogalla.tripbook.data.LocationContract;
import com.nikogalla.tripbook.models.Location;
import com.nikogalla.tripbook.prefs.PreferencesUtils;
import com.nikogalla.tripbook.utils.DistanceUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.net.URL;

/**
 * Created by Nicola on 2017-02-10.
 */

public class WidgetLocationNearbyRemoteViewsService extends RemoteViewsService {
    private static final String TAG = WidgetLocationNearbyRemoteViewsService.class.getSimpleName();

    private static final String[] LOCATION_COLUMNS = {
            LocationContract.LocationEntry._ID,
            LocationContract.LocationEntry.COLUMN_KEY,
            LocationContract.LocationEntry.COLUMN_NAME,
            LocationContract.LocationEntry.COLUMN_DISTANCE,
            LocationContract.LocationEntry.COLUMN_COMMENT_COUNT,
            LocationContract.LocationEntry.COLUMN_RATE,
            LocationContract.LocationEntry.COLUMN_RATE_COUNT,
            LocationContract.LocationEntry.COLUMN_PICTURE_URL,
    };
    // projection indices
    private static final int INDEX_ID = 0;
    private static final int INDEX_KEY = 1;
    private static final int INDEX_NAME = 2;
    private static final int INDEX_DISTANCE = 3;
    private static final int INDEX_COMMENT_NUMBER = 4;
    private static final int INDEX_RATE = 5;
    private static final int INDEX_RATE_COUNT = 6;
    private static final int INDEX_PICTURE_URL = 7;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsService.RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
                // Nothing to do
                Log.v(TAG,"On create");
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();
                Uri locationUri = LocationContract.LocationEntry.CONTENT_URI;
                data = getContentResolver().query(locationUri,LOCATION_COLUMNS,null,null, LocationContract.LocationEntry.COLUMN_DISTANCE + " ASC "+" LIMIT 4");
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(), R.layout.item_location_widget);

                String imageUri = data.getString(INDEX_PICTURE_URL);
                try{
                    URL url = new URL(imageUri);
                    Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    views.setImageViewBitmap(R.id.ivLocationImage,bmp);
                }catch (Exception e){
                    Log.d(TAG,"Cannot load image " + e.getMessage());
                }
                String locationName = data.getString(INDEX_NAME);
                views.setTextViewText(R.id.tvLocationName, locationName);

                int distance = data.getInt(INDEX_DISTANCE);
                String unit = new PreferencesUtils(getBaseContext()).getPreferredDistanceUnit();
                String compactDistance;
                if (unit.matches("km")){
                    compactDistance = String.valueOf(DistanceUtils.getDistanceInKm(distance));
                }else{
                    compactDistance = String.valueOf(DistanceUtils.getDistanceInMiles(distance));
                }
                views.setTextViewText(R.id.tvLocationDistance, compactDistance);
                int commentsNumber = data.getInt(INDEX_COMMENT_NUMBER);
                views.setTextViewText(R.id.tvLocationCommentsNumber, String.valueOf(commentsNumber));

                float locationRate = data.getFloat(INDEX_RATE);
                String rateString = Location.getRateString(getBaseContext(),locationRate);
                views.setTextViewText(R.id.tvLocationRates,rateString);

                float rateCount = data.getFloat(INDEX_RATE_COUNT);

                String locationKey = data.getString(INDEX_KEY);
                // On list item click
                final Intent fillInIntent = new Intent();
                fillInIntent.putExtra(getString(R.string.location_key_id),locationKey);
                fillInIntent.putExtra(getString(R.string.location_name_id),locationName);
                views.setOnClickFillInIntent(R.id.llItemLocationContainer, fillInIntent);

                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.item_location_widget);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(INDEX_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
