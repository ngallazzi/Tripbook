package com.nikogalla.tripbook;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.GridView;

import com.google.firebase.auth.FirebaseAuth;
import com.nikogalla.tripbook.data.LocationContract;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UserAccountInfoActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private final static String TAG = UserAccountInfoActivity.class.getSimpleName();
    @BindView(R.id.tbUserInfos)
    Toolbar tbUserInfos;
    @BindView(R.id.gvUserAccountsInfos)
    GridView gvUserInfos;
    Context mContext;
    Uri mUserByUIDUri;
    private UserAccountInfoAdapter accountInfosAdapter;

    private static final int USER_LOADER = 1;
    private static final String[] USER_COLUMNS = {
            LocationContract.UserEntry.TABLE_NAME + "." + LocationContract.UserEntry._ID,
            LocationContract.UserEntry.TABLE_NAME + "." + LocationContract.UserEntry.COLUMN_UID,
            LocationContract.UserEntry.TABLE_NAME + "." + LocationContract.UserEntry.COLUMN_NAME,
            LocationContract.UserEntry.TABLE_NAME + "." + LocationContract.UserEntry.COLUMN_EMAIL,
            LocationContract.UserEntry.TABLE_NAME + "." + LocationContract.UserEntry.COLUMN_PROVIDER,
            LocationContract.UserEntry.TABLE_NAME + "." + LocationContract.UserEntry.COLUMN_PICTURE_URL
    };
    public static final int COL_ID = 0;
    public static final int COL_UID = 1;
    public static final int COL_NAME = 2;
    public static final int COL_EMAIL = 3;
    public static final int COL_PROVIDER = 4;
    public static final int COL_PICTURE_URL = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_accounts_info);
        ButterKnife.bind(this);
        mContext = this;
        tbUserInfos.setTitle(getString(R.string.account_infos));
        setSupportActionBar(tbUserInfos);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportLoaderManager().initLoader(0, null, this);
        String userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mUserByUIDUri = LocationContract.UserEntry.buildUserUriWithUID(userUID);
        Cursor cur = mContext.getContentResolver().query(mUserByUIDUri,USER_COLUMNS,null, null, null);
        accountInfosAdapter = new UserAccountInfoAdapter(mContext,cur,0);
        gvUserInfos.setAdapter(accountInfosAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try{


        }catch (Exception e){

        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader;
        if (id == USER_LOADER){
            loader = new CursorLoader(this,mUserByUIDUri,USER_COLUMNS,null,null,null);
            return loader;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        try{
            // Populating details view
            if (loader.getId() == USER_LOADER){
                accountInfosAdapter.swapCursor(data);
            }

        }catch (Exception e){
            Log.e(TAG, "Fail on load finished: " + e.getMessage());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == USER_LOADER){
            accountInfosAdapter.swapCursor(null);
        }
    }
}
