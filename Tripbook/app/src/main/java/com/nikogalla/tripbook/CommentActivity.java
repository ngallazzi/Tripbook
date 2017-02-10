package com.nikogalla.tripbook;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nikogalla.tripbook.data.FirebaseHelper;
import com.nikogalla.tripbook.models.Comment;
import com.nikogalla.tripbook.models.Location;
import com.nikogalla.tripbook.utils.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Nicola on 2017-01-30.
 */

public class CommentActivity extends AppCompatActivity {
    private static final String TAG = CommentActivity.class.getSimpleName();
    @BindView(R.id.tbAddComment)
    Toolbar tbAddComment;
    @BindView(R.id.rvLocationComments)
    RecyclerView rvLocationComments;
    @BindView(R.id.etAddComment)
    EditText etAddComment;
    @BindView(R.id.ibAddComment)
    ImageButton ibAddComment;
    Location mLocation;
    private ArrayList<Comment> mCommentsArrayList;
    private LinearLayoutManager mLayoutManager;
    private CommentAdapter mCommentsAdapter;
    private Context mContext;
    FirebaseDatabase mDatabase;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        mLocation = getIntent().getParcelableExtra(getString(R.string.location_id));
        mCommentsArrayList = new ArrayList<>();
        mContext = this;
        ButterKnife.bind(this);
        tbAddComment.setTitle(getString(R.string.add_comment));
        setSupportActionBar(tbAddComment);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setStackFromEnd(true);
        rvLocationComments.setLayoutManager(mLayoutManager);
        // specify an adapter (see also next example)
        mCommentsAdapter = new CommentAdapter(mCommentsArrayList,mContext);
        rvLocationComments.setAdapter(mCommentsAdapter);
        mDatabase = FirebaseHelper.getDatabase();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mCommentsArrayList.clear();
        ibAddComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeNewComment(String.valueOf(etAddComment.getText()));
            }
        });
        DatabaseReference ref = mDatabase.getReference("/locations/" + mLocation.getKey() + "/" +Comment.COMMENTS_TABLE_NAME+"/");
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Comment comment = dataSnapshot.getValue(Comment.class);
                Log.v(TAG,comment.text);
                mCommentsArrayList.add(comment);
                mCommentsAdapter.notifyDataSetChanged();
                rvLocationComments.smoothScrollToPosition(mCommentsAdapter.getItemCount() - 1);
                hideKeyboard();
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

    public void hideKeyboard(){
        try{
            etAddComment.getText().clear();
            etAddComment.clearFocus();
            InputMethodManager inputManager =
                    (InputMethodManager) mContext.
                            getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(
                    this.getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }catch (Exception e){

        }
    }

    private void writeNewComment(String text){
        Date now = new Date();
        String nowString = DateUtils.getUTCDateStringFromdate(now);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Comment commentToAdd = new Comment(nowString,text,user.getUid(),user.getDisplayName(),String.valueOf(user.getPhotoUrl()));
        Map<String, Object> commentValues = commentToAdd.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        String commentsKey = mDatabase.getReference().child("comments").push().getKey();
        childUpdates.put("/locations/" + mLocation.getKey() + "/" + Comment.COMMENTS_TABLE_NAME + "/" + commentsKey,commentValues);
        mDatabase.getReference().updateChildren(childUpdates);
    }
}
