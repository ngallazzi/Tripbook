package com.nikogalla.tripbook.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import com.nikogalla.tripbook.R;

/**
 * Created by Nicola on 2017-02-02.
 */

public class StatusSnackBars {

    public static Snackbar getStatusSnackBar(String message, View attachedView){
        Snackbar snackbar = Snackbar.make(attachedView, message, Snackbar.LENGTH_SHORT);
        return snackbar;
    }

    public static Snackbar getErrorSnackBar(String message, View attachedView){
        final Snackbar snackbar = Snackbar.make(attachedView, message, Snackbar.LENGTH_LONG);
        return snackbar;
    }

    public static Snackbar getErrorSnackBar(String message, View attachedView, final Activity callingActivity){
        final Snackbar snackbar = Snackbar.make(attachedView, message, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(callingActivity.getBaseContext().getString(R.string.settings), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
                callingActivity.startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
            }
        });
        return snackbar;
    }
}
