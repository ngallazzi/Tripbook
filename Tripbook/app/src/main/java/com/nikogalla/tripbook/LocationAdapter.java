package com.nikogalla.tripbook;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nikogalla.tripbook.models.Location;
import com.nikogalla.tripbook.prefs.PreferencesUtils;
import com.nikogalla.tripbook.utils.DistanceUtils;
import com.nikogalla.tripbook.utils.NetworkUtils;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Nicola on 2017-01-27.
 */

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ViewHolder> {
    private final String TAG = LocationAdapter.class.getSimpleName();
    private ArrayList<Location> mLocations;
    private Context mContext;
    private AroundYouActivity mActivity;
    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public LinearLayout llItemLocationContainer;
        public TextView tvLocationName,tvLocationComments,tvLocationRates;
        public ImageView ivLocation,ibLocationShare;
        public ViewHolder(View v) {
            super(v);
            llItemLocationContainer = (LinearLayout) v.findViewById(R.id.llItemLocationContainer);
            tvLocationName = (TextView) v.findViewById(R.id.tvLocationName);
            tvLocationComments = (TextView) v.findViewById(R.id.tvLocationComments);
            tvLocationRates = (TextView) v.findViewById(R.id.tvLocationRates);
            ivLocation = (ImageView) v.findViewById(R.id.ivLocation);
            ibLocationShare = (ImageButton) v.findViewById(R.id.ibLocationShare);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public LocationAdapter(ArrayList<Location> myDataset, Context context) {
        mLocations = myDataset;
        mContext = context;
        mActivity= (AroundYouActivity) context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public LocationAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View layout = (View) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_location, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(layout);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final Location location = mLocations.get(position);
        setImage(location,holder);
        setRates(location,holder);
        setCommentsCount(location,holder);
        holder.llItemLocationContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext,LocationDetailsActivity.class);
                intent.putExtra(mContext.getString(R.string.location_key_id),location.getKey());
                intent.putExtra(mContext.getString(R.string.location_name_id),location.name);
                intent.putExtra(mContext.getString(R.string.location_id),location);
                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation(mActivity,(View)holder.ivLocation, mContext.getString(R.string.shared_element_transition_id));
                mContext.startActivity(intent, options.toBundle());
            }
        });
        holder.ibLocationShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get access to the URI for the bitmap
                Uri bmpUri = getLocalBitmapUri(holder.ivLocation);
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, mContext.getString(R.string.share_text,location.name));
                intent.putExtra(Intent.EXTRA_STREAM, bmpUri);
                intent.setType("image/*");
                mContext.startActivity(Intent.createChooser(intent, mContext.getString(R.string.share_intent)));
            }
        });
    }

    public void setImage(Location location, ViewHolder holder){
        String unit = new PreferencesUtils(mContext).getPreferredDistanceUnit();
        String compactDistance;
        if (unit.matches("km")){
            compactDistance = String.valueOf(DistanceUtils.getDistanceInKm(location.distance));
        }else{
            compactDistance = String.valueOf(DistanceUtils.getDistanceInMiles(location.distance));
        }
        holder.tvLocationName.setText(location.name + " (" +compactDistance+")");
        if (NetworkUtils.isOnline(mContext)){
            Picasso.with(mContext).load(location.getMainPhotoUrl()).into(holder.ivLocation);
        }else{
            Picasso.with(mContext).load(location.getMainPhotoUrl()).networkPolicy(NetworkPolicy.OFFLINE).into(holder.ivLocation);
            Log.v(TAG,"no connection, loading local images");
        }
    }


    public void setRates(Location location, ViewHolder holder){
        try{
            holder.tvLocationRates.setText(location.getRateString(mContext));
        }catch (Exception e){
            Log.d(TAG,"No rates for location: " + location.name + " " + e.getMessage());
        }
    }

    public void setCommentsCount(Location location, ViewHolder holder){
        try{
            holder.tvLocationComments.setText(String.valueOf(location.comments.size()));
        }catch (Exception e){
            Log.d(TAG,"No rates for location: " + location.name + " " + e.getMessage());
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (mLocations!=null){
            return mLocations.size();
        }else{
            return 0;
        }
    }

    // Returns the URI path to the Bitmap displayed in specified ImageView
    public Uri getLocalBitmapUri(ImageView imageView) {
        // Extract Bitmap from ImageView drawable
        Drawable drawable = imageView.getDrawable();
        Bitmap bmp = null;
        if (drawable instanceof BitmapDrawable){
            bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        } else {
            return null;
        }
        // Store image to default external storage directory
        Uri bmpUri = null;
        try {
            // Use methods on Context to access package-specific directories on external storage.
            // This way, you don't need to request external read/write permission.
            // See https://youtu.be/5xVh-7ywKpE?t=25m25s
            File file =  new File(mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image_" + System.currentTimeMillis() + ".png");
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 75, out);
            out.close();
            // **Warning:** This will fail for API >= 24, use a FileProvider as shown below instead.
            bmpUri = Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }
}
