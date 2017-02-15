package com.nikogalla.tripbook.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.nikogalla.tripbook.models.Location;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;

/**
 * Created by Nicola on 2017-02-15.
 */

public class ImageUtils {
    private final static String TAG = ImageUtils.class.getSimpleName();
    private Location mLocation;
    private Context mContext;
    private String mFilename;
    public ImageUtils(Location location, Context context) {
        mLocation = location;
        mContext = context;
        mFilename = mLocation.key + ".jpg";
    }

    public void saveImageLocally(){
        try {
            if (!fileExists()){
                new DownloadImageTask().execute(mFilename);
            }
        }catch (Exception e){
            Log.d(TAG,"Error saving: " + e.getMessage() + " Location: " + mLocation.name);
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            try{
                String filename = params[0];
                URL imageurl = new URL(mLocation.getMainPhotoUrl());
                Bitmap bitmap = BitmapFactory.decodeStream(imageurl.openConnection().getInputStream());
                FileOutputStream ostream = mContext.openFileOutput(filename, Context.MODE_PRIVATE);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 75, ostream);
                Log.v(TAG,"Saved filename: " +filename + ", Location: " + mLocation.name);
                ostream.close();
            }catch (Exception e){
                Log.d(TAG,"Error saving: " + e.getMessage() + " Location: " + mLocation.name);
            }
            return null;
        }
    }


    public Bitmap getLocalBitmapForLocation(){
        try{

            Log.d(TAG,"Loading image for location: " + mLocation.name);
            FileInputStream fin = mContext.openFileInput(mFilename);
            Bitmap bitmap = BitmapFactory.decodeStream(fin);
            fin.close();
            Log.d(TAG,"Loaded");
            return bitmap;
        }catch (Exception e){
            Log.d(TAG,"Error: " + e.getMessage());
            return null;
        }
    }

    public boolean fileExists() {
        File file = mContext.getFileStreamPath(mFilename);
        if(file == null || !file.exists()) {
            return false;
        }
        return true;
    }
}
