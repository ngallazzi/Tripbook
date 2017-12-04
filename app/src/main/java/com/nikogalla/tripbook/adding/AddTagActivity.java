package com.nikogalla.tripbook.adding;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nikogalla.tripbook.AroundYouActivity;
import com.nikogalla.tripbook.R;
import com.nikogalla.tripbook.data.FirebaseHelper;
import com.nikogalla.tripbook.models.Location;
import com.nikogalla.tripbook.models.Tag;
import com.nikogalla.tripbook.utils.ImageUtils;
import com.nikogalla.tripbook.utils.LocationUtils;
import com.nikogalla.tripbook.utils.StatusSnackBars;
import com.pchmn.materialchips.ChipsInput;
import com.pchmn.materialchips.model.ChipInterface;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Nicola on 2017-06-26.
 */

public class AddTagActivity extends AppCompatActivity {
    private final String TAG = AddTagActivity.class.getSimpleName();
    @BindView(R.id.ciSelectTag)
    ChipsInput ciNewTag;
    @BindView(R.id.clActivityAddTagContainer)
    CoordinatorLayout clActivityAddTagContainer;
    private FirebaseDatabase mDatabase;
    private Context mContext;
    private List<Tag> mTagList;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_tag);
        ButterKnife.bind(this);
        mDatabase = FirebaseHelper.getDatabase();
        mTagList = new ArrayList<>();
    }

    @Override
    protected void onStart() {
        super.onStart();

        DatabaseReference ref = mDatabase.getReference(Tag.TAGS_TABLE_NAME);
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Tag chip = dataSnapshot.getValue(Tag.class);
                // add contact to the list
                mTagList.add(chip);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.v(TAG,"Child changed");
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.v(TAG,"Child removed");
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.v(TAG,"Child moved");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                StatusSnackBars.getErrorSnackBar(getString(R.string.database_error),clActivityAddTagContainer,AddTagActivity.this).show();
            }
        });
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("We're done loading the initial "+dataSnapshot.getChildrenCount()+" items");
                // pass the ContactChip list
                ciNewTag.setFilterableList(mTagList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        ciNewTag.addChipsListener(new ChipsInput.ChipsListener() {
            @Override
            public void onChipAdded(ChipInterface chip, int newSize) {
                // chip added
                // newSize is the size of the updated selected chip list
            }

            @Override
            public void onChipRemoved(ChipInterface chip, int newSize) {
                // chip removed
                // newSize is the size of the updated selected chip list
            }

            @Override
            public void onTextChanged(CharSequence text) {
                // text changed
            }
        });
    }
}
