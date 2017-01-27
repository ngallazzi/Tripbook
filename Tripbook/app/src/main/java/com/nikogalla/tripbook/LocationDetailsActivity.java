package com.nikogalla.tripbook;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.ornolfr.ratingview.RatingView;
import com.nikogalla.tripbook.models.Comment;
import com.nikogalla.tripbook.models.Location;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LocationDetailsActivity extends AppCompatActivity {
    private final String TAG = LocationDetailsActivity.class.getSimpleName();
    private Context mContext;
    private Location mLocation;
    @BindView(R.id.ivLocationPicture)
    ImageView ivLocationPicture;
    @BindView(R.id.rvLocationRatings)
    RatingView rtvLocationRatings;
    @BindView(R.id.tvLocationRating)
    TextView tvLocationRating;
    @BindView(R.id.ivLocationReviews)
    ImageButton ivLocationReviews;
    @BindView(R.id.tvLocationDescription)
    TextView tvLocationDescription;
    @BindView(R.id.rvLocationReviews)
    RecyclerView rvLocationReviews;
    private CommentAdapter mReviewAdapter;
    private ArrayList<Comment> mCommentsArrayList;
    private LinearLayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_details);
        ButterKnife.bind(this);
        mContext = this;
        mLocation = getIntent().getParcelableExtra(getString(R.string.location_id));
        mCommentsArrayList = new ArrayList<>();
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        rvLocationReviews.setLayoutManager(mLayoutManager);
        // specify an adapter (see also next example)
        mReviewAdapter = new CommentAdapter(mCommentsArrayList,mContext);
        rvLocationReviews.setAdapter(mReviewAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadLocationPicture();
        loadLocationInfos();
        loadLocationComments();
    }

    private void loadLocationInfos(){
        setTitle(mLocation.name);
        tvLocationDescription.setText(mLocation.description);
        float rating = mLocation.getRate();
        if (rating>0){
            tvLocationRating.setText(mLocation.getRateString(mContext));
        }

    }

    private void loadLocationPicture(){
        try{
            Picasso.with(mContext).load(mLocation.photoUrls.get(0)).into(ivLocationPicture);
        }catch (Exception e){
            Log.d(TAG,"No photo for location: " + mLocation.name + " " + e.getMessage());
        }
    }


    private void loadLocationComments(){
        for (Comment c: mLocation.comments){
            mCommentsArrayList.add(c);
        }
        mReviewAdapter.notifyDataSetChanged();
    }
}
