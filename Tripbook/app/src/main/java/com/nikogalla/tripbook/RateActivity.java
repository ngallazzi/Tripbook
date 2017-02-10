package com.nikogalla.tripbook;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.github.ornolfr.ratingview.RatingView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nikogalla.tripbook.data.FirebaseHelper;
import com.nikogalla.tripbook.models.Location;
import com.nikogalla.tripbook.models.Rate;
import com.nikogalla.tripbook.utils.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RateActivity extends AppCompatActivity {
    private final String TAG = RateActivity.class.getSimpleName();
    @BindView(R.id.tbAddRate)
    Toolbar tbAddRate;
    @BindView(R.id.rtvAddRate)
    RatingView rtvAddRate;
    @BindView(R.id.rvLocationRates)
    RecyclerView rvLocationRates;
    Location mLocation;
    private ArrayList<Rate> mRatesArrayList;
    private LinearLayoutManager mLayoutManager;
    private RateAdapter mRatesAdapter;
    private Context mContext;
    FirebaseDatabase mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate);
        mLocation = getIntent().getParcelableExtra(getString(R.string.location_id));
        mRatesArrayList = new ArrayList<>();
        ButterKnife.bind(this);
        mContext = this;
        tbAddRate.setTitle(getString(R.string.add_rate));
        setSupportActionBar(tbAddRate);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        rvLocationRates.setLayoutManager(mLayoutManager);
        // specify an adapter (see also next example)
        mRatesAdapter = new RateAdapter(mRatesArrayList,mContext);
        rvLocationRates.setAdapter(mRatesAdapter);
        mDatabase = FirebaseHelper.getDatabase();
    }

    @Override
    protected void onStart() {
        super.onStart();
        DatabaseReference ref = mDatabase.getReference("/locations/" + mLocation.getKey() + "/" +Rate.RATES_TABLE_NAME+"/");
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Rate rate = dataSnapshot.getValue(Rate.class);
                Log.v(TAG,"Rate added: " + String.valueOf(rate.rate));
                mRatesArrayList.add(rate);
                mRatesAdapter.notifyDataSetChanged();
                rvLocationRates.smoothScrollToPosition(mRatesAdapter.getItemCount() - 1);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        rtvAddRate.setOnRatingChangedListener(new RatingView.OnRatingChangedListener() {
            @Override
            public void onRatingChange(float oldRating, float newRating) {
                int rate = (int) newRating;
                writeNewRate(rate);
            }
        });
    }

    private void writeNewRate(int rate){
        Date now = new Date();
        String nowString = DateUtils.getUTCDateStringFromdate(now);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Rate rateToAdd = new Rate(rate,nowString,user.getUid(),user.getDisplayName(),user.getPhotoUrl().toString());
        Map<String, Object> rateValues = rateToAdd.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        String commentsKey = mDatabase.getReference().child("rates").push().getKey();
        childUpdates.put("/locations/" + mLocation.getKey() + "/" + Rate.RATES_TABLE_NAME + "/" + commentsKey,rateValues);
        mDatabase.getReference().updateChildren(childUpdates);
    }
}
