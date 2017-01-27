package com.nikogalla.tripbook.models;

import java.util.Date;

/**
 * Created by Nicola on 2017-01-27.
 */

public class User {
    public int id;
    public String name;
    public String pictureUrl;
    public String createdOn;

    public User() {

    }

    public User(String createdOn, int id, String name, String pictureUrl) {
        this.createdOn = createdOn;
        this.id = id;
        this.name = name;
        this.pictureUrl = pictureUrl;
    }
}
