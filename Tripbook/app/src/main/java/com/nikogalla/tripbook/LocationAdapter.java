package com.nikogalla.tripbook;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nikogalla.tripbook.models.Location;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Nicola on 2017-01-27.
 */

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ViewHolder> {
    private final String TAG = LocationAdapter.class.getSimpleName();
    private ArrayList<Location> mLocations;
    private Context mContext;
    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public LinearLayout llItemLocationContainer;
        public TextView tvLocationName,tvLocationComments,tvLocationRates;
        public ImageView ivLocation,ivLocationShare;
        public ViewHolder(View v) {
            super(v);
            llItemLocationContainer = (LinearLayout) v.findViewById(R.id.llItemLocationContainer);
            tvLocationName = (TextView) v.findViewById(R.id.tvLocationName);
            tvLocationComments = (TextView) v.findViewById(R.id.tvLocationComments);
            tvLocationRates = (TextView) v.findViewById(R.id.tvLocationRates);
            ivLocation = (ImageView) v.findViewById(R.id.ivLocation);
            ivLocationShare = (ImageView) v.findViewById(R.id.ivLocationShare);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public LocationAdapter(ArrayList<Location> myDataset, Context context) {
        mLocations = myDataset;
        mContext = context;
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
    public void onBindViewHolder(ViewHolder holder, int position) {

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
                intent.putExtra(mContext.getString(R.string.location_id),location);
                mContext.startActivity(intent);
            }
        });
    }

    public void setImage(Location location, ViewHolder holder){
        holder.tvLocationName.setText(location.name);
        try{
            Picasso.with(mContext).load(location.getMainPhotoUrl()).into(holder.ivLocation);
        }catch (Exception e){
            Log.d(TAG,"No photo for location: " + location.name + " " + e.getMessage());
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
}
