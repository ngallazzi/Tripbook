package com.nikogalla.tripbook;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nikogalla.tripbook.models.Location;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AroundYouActivity extends AppCompatActivity {
    private final String TAG = AroundYouActivity.class.getSimpleName();

    @BindView(R.id.rvLocations) RecyclerView mRvLocations;
    @BindView(R.id.fabAddLocation) FloatingActionButton fabAddLocation;
    private LocationAdapter mLocationsAdapter;
    private ArrayList<Location> mLocationsArrayList;
    private LinearLayoutManager mLayoutManager;
    FirebaseDatabase mDatabase;
    private Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_around_you);
        mContext = this;
        ButterKnife.bind(this);
        mDatabase = FirebaseDatabase.getInstance();
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRvLocations.setHasFixedSize(true);
        mLocationsArrayList = new ArrayList<>();
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRvLocations.setLayoutManager(mLayoutManager);
        // specify an adapter (see also next example)
        mLocationsAdapter = new LocationAdapter(mLocationsArrayList,mContext);
        mRvLocations.setAdapter(mLocationsAdapter);
        fabAddLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext,AddLocationActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mLocationsArrayList.isEmpty()){
            getLocationsByProximity(null,null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void getLocationsByProximity(Double longitude, Double latitude){
        DatabaseReference ref = mDatabase.getReference(Location.LOCATION_TABLE_NAME);
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Location location = dataSnapshot.getValue(Location.class);
                Log.v(TAG,"Location key: " +  dataSnapshot.getKey());
                location.setKey(dataSnapshot.getKey());
                System.out.println("Location: " + location.name);
                System.out.println("Address: " + location.address);
                mLocationsArrayList.add(location);
                mLocationsAdapter.notifyDataSetChanged();
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
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
