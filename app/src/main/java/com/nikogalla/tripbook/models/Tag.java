package com.nikogalla.tripbook.models;

import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.pchmn.materialchips.model.ChipInterface;

/**
 * Created by Nicola on 2017-06-26.
 */

public class Tag implements ChipInterface {
    public static final String TAGS_TABLE_NAME = "tags";
    public String key;
    public String label;
    public String avatarUrl;

    @Override
    public Object getId() {
        return key;
    }

    @Override
    public Uri getAvatarUri() {
        Uri uri = Uri.parse(avatarUrl);
        return uri;
    }

    @Override
    public Drawable getAvatarDrawable() {
        return null;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getInfo() {
        return null;
    }
}
