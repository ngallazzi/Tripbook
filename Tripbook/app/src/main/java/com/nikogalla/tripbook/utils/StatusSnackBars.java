package com.nikogalla.tripbook.utils;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.nikogalla.tripbook.R;

/**
 * Created by Nicola on 2017-02-02.
 */

public class StatusSnackBars {
    public static Snackbar getStatusSnackBar(String message, View attachedView){
        Snackbar snackbar = Snackbar.make(attachedView, message, Snackbar.LENGTH_LONG);
        return snackbar;
    }

    public static Snackbar getErrorSnackBar(String message, View attachedView){
        final Snackbar snackbar = Snackbar.make(attachedView, message, Snackbar.LENGTH_INDEFINITE);
        return snackbar;
    }

    public static Snackbar getErrorSnackBarWithRetryAction(Context context, String errorMessage, View attachedView, View.OnClickListener listener){
        return  StatusSnackBars.getErrorSnackBar(errorMessage,attachedView)
                .setActionTextColor(ContextCompat.getColor(context, R.color.accent))
                .setAction(context.getString(R.string.retry), listener);
    }
}
