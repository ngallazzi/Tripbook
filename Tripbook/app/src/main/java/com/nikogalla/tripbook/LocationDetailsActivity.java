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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nikogalla.tripbook.models.Comment;
import com.nikogalla.tripbook.models.Photo;
import com.nikogalla.tripbook.models.Rate;
import com.nikogalla.tripbook.models.Location;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.R.attr.key;
import static com.nikogalla.tripbook.R.string.rate;

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
    DatabaseReference mDatabase;

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
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadLocationPicture();
        loadLocationInfos();
        loadLocationComments();
        rtvLocationRatings.setOnRatingChangedListener(new RatingView.OnRatingChangedListener() {
            @Override
            public void onRatingChange(float oldRating, float newRating) {
                writeNewRate(newRating);
            }
        });
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
            String photoUrl = mLocation.getMainPhotoUrl();
            Picasso.with(mContext).load(photoUrl).into(ivLocationPicture);
        }catch (Exception e){
            Log.d(TAG,"No photo for location: " + mLocation.name + " " + e.getMessage());
        }
    }


    private void loadLocationComments(){
        for (Map.Entry<String,Comment> entry : mLocation.comments.entrySet()) {
           mCommentsArrayList.add(entry.getValue());
        }
        mReviewAdapter.notifyDataSetChanged();
    }

    private void writeNewRate(float newRate){
        Date now = new Date();
        String nowString = DateUtils.getUTCDateStringFromdate(now);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Rate rateToAdd = new Rate((int) newRate,nowString, userId);
        Map<String, Object> rateValues = rateToAdd.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        String ratesKey = mDatabase.child("rates").push().getKey();
        childUpdates.put("/locations/" + mLocation.getKey() + "/rates/" + ratesKey,rateValues);
        mDatabase.updateChildren(childUpdates);
    }
}
