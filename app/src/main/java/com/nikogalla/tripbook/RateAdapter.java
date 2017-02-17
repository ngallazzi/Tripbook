package com.nikogalla.tripbook;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.ornolfr.ratingview.RatingView;
import com.nikogalla.tripbook.models.Rate;
import com.nikogalla.tripbook.utils.NetworkUtils;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.nikogalla.tripbook.utils.DateUtils.getHumanReadableDateString;

/**
 * Created by Nicola on 2017-01-27.
 */

public class RateAdapter extends RecyclerView.Adapter<RateAdapter.ViewHolder> {
    private final String TAG = RateAdapter.class.getSimpleName();
    private ArrayList<Rate> mRates;
    private Context mContext;
    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public CircleImageView civRateAuthor;
        public RatingView rtvRate;
        public TextView tvRateDate;
        public ViewHolder(View v) {
            super(v);
            civRateAuthor = (CircleImageView) v.findViewById(R.id.civRateAuthor);
            rtvRate = (RatingView) v.findViewById(R.id.rtvRate);
            tvRateDate = (TextView) v.findViewById(R.id.tvRateDate);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public RateAdapter(ArrayList<Rate> myDataset, Context context) {
        mRates = myDataset;
        mContext = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RateAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        // create a new view
        View layout = (View) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rate, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(layout);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final Rate rate = mRates.get(position);
        setImage(rate,holder);
        setRate(rate,holder);
        setText(rate,holder);
    }

    public void setImage(Rate rate, ViewHolder holder){
        if (NetworkUtils.isOnline(mContext)){
            Picasso.with(mContext).load(rate.userPictureUrl).into(holder.civRateAuthor);
        }else{
            Picasso.with(mContext).load(rate.userPictureUrl).networkPolicy(NetworkPolicy.OFFLINE).into(holder.civRateAuthor);
            Log.v(TAG,"no connection, loading local user image");
        }
    }

    public void setRate(Rate rate,ViewHolder holder){
        holder.rtvRate.setRating((int) rate.rate);
    }

    public void setText(Rate rate, ViewHolder holder){
        try{
            String date = getHumanReadableDateString(rate.createdAt);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                holder.tvRateDate.setText(Html.fromHtml("<b>" + rate.userName + "</b> " + date, Html.FROM_HTML_MODE_COMPACT));
            }else{
                holder.tvRateDate.setText(Html.fromHtml("<b>" + rate.userName + "</b> " + date));
            }
        }catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (mRates!=null){
            return mRates.size();
        }else{
            return 0;
        }
    }
}
