package com.nikogalla.tripbook.models;

import java.util.Date;

/**
 * Created by Nicola on 2017-01-27.
 */

public class User {
    public String UID;
    public String email;
    public String provider;
    public String name;
    public String pictureUrl;

    public User() {

    }

    public User(String email, String name, String pictureUrl, String provider, String UID) {
        this.email = email;
        this.name = name;
        this.pictureUrl = pictureUrl;
        this.provider = provider;
        this.UID = UID;
    }
}
