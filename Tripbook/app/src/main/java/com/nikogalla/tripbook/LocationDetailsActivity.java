package com.nikogalla.tripbook;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    @BindView(R.id.tbLocationDetails)
    Toolbar tbLocationDetails;
    @BindView(R.id.ivLocationPicture)
    ImageView ivLocationPicture;
    @BindView(R.id.tvLocationRating)
    TextView tvLocationRating;
    @BindView(R.id.tvLocationComments)
    TextView tvLocationComments;
    @BindView(R.id.tvLocationDescription)
    TextView tvLocationDescription;
    @BindView(R.id.tvLocationAddress)
    TextView tvLocationAddress;
    @BindView(R.id.llComments)
    LinearLayout llComments;
    @BindView(R.id.llRates)
    LinearLayout llRates;
    DatabaseReference mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_details);
        ButterKnife.bind(this);
        mContext = this;
        mLocation = getIntent().getParcelableExtra(getString(R.string.location_id));
        mDatabase = FirebaseDatabase.getInstance().getReference();
        tbLocationDetails.setTitle(mLocation.name);
        setSupportActionBar(tbLocationDetails);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        tvLocationAddress.setText(mLocation.address);
        loadLocationPicture();
        loadLocationInfos();
        llRates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext,RateActivity.class);
                intent.putExtra(getString(R.string.location_id),mLocation);
                startActivity(intent);
            }
        });
        llComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext,CommentActivity.class);
                intent.putExtra(getString(R.string.location_id),mLocation);
                startActivity(intent);
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
        if (mLocation.comments.size()>0){
            tvLocationComments.setText(mLocation.comments.size() + " " + mContext.getString(R.string.comments));
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
}
