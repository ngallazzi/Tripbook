package com.nikogalla.tripbook;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nikogalla.tripbook.models.Comment;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Nicola on 2017-01-27.
 */

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    private final String TAG = CommentAdapter.class.getSimpleName();
    private ArrayList<Comment> mComments;
    private Context mContext;
    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public CircleImageView civCommentAuthor;
        public TextView tvComment,tvAuthorName;
        public ViewHolder(View v) {
            super(v);
            civCommentAuthor = (CircleImageView) v.findViewById(R.id.civCommentAuthor);
            tvComment = (TextView) v.findViewById(R.id.tvComment);
            tvAuthorName = (TextView) v.findViewById(R.id.tvAuthorName);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public CommentAdapter(ArrayList<Comment> myDataset, Context context) {
        mComments = myDataset;
        mContext = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public CommentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        // create a new view
        View layout = (View) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(layout);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final Comment comment = mComments.get(position);
        setImage(comment,holder);
        setText(comment,holder);
    }

    public void setImage(Comment comment, ViewHolder holder){
        try{
            Picasso.with(mContext).load(comment.userPictureUrl).into(holder.civCommentAuthor);
        }catch (Exception e){
            Log.d(TAG,"No photo for user: " + comment.userId + " " + e.getMessage());
        }
    }

    public void setText(Comment comment, ViewHolder holder){
        holder.tvAuthorName.setText(comment.userName);
        holder.tvComment.setText(comment.text);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (mComments!=null){
            return mComments.size();
        }else{
            return 0;
        }
    }
}
