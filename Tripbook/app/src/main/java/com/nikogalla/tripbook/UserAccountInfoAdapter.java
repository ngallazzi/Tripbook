package com.nikogalla.tripbook;

/**
 * Created by Nicola on 2017-02-15.
 */

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Nicola on 2016-04-21.
 */
public class UserAccountInfoAdapter extends CursorAdapter {
    private final String TAG = UserAccountInfoAdapter.class.getSimpleName();
    private Context mContext;

    public UserAccountInfoAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_account_infos, parent, false);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = new ViewHolder(view);
        String userName = cursor.getString(UserAccountInfoActivity.COL_NAME);
        holder.tvUserName.setText(userName);

        String email = cursor.getString(UserAccountInfoActivity.COL_EMAIL);
        holder.tvUserMail.setText(email);

        String imageUrl = cursor.getString(UserAccountInfoActivity.COL_PICTURE_URL);
        Picasso.with(mContext).load(imageUrl).into(holder.ivUserInfoIcon);
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.civUserImage)
        CircleImageView ivUserInfoIcon;
        @BindView(R.id.tvUserName)
        TextView tvUserName;
        @BindView(R.id.tvUserMail)
        TextView tvUserMail;
        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }


}