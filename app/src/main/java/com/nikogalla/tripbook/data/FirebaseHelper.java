package com.nikogalla.tripbook.data;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Nicola on 2017-02-06.
 */

public class FirebaseHelper {
    private static FirebaseDatabase mDatabase;

    public static FirebaseDatabase getDatabase() {
        if (mDatabase == null) {
            mDatabase = FirebaseDatabase.getInstance();
            mDatabase.setPersistenceEnabled(true);
        }
        return mDatabase;
    }
}
