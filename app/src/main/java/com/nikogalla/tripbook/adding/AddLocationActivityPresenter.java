package com.nikogalla.tripbook.adding;

/**
 * Created by Nicola on 2017-03-17.
 */

public class AddLocationActivityPresenter {
    AddLocationActivityView view;

    public AddLocationActivityPresenter(AddLocationActivityView view) {
        this.view = view;
    }

    public void addPictureImageViewClicked(){
        view.addPicture();
    }
}
