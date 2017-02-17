package com.nikogalla.tripbook.utils;

import android.content.Context;
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
        View view = snackbar.getView();
        TextView textView = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        return snackbar;
    }
}
