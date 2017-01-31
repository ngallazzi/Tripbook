package com.nikogalla.tripbook;

import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;

import com.seatgeek.placesautocomplete.DetailsCallback;
import com.seatgeek.placesautocomplete.OnPlaceSelectedListener;
import com.seatgeek.placesautocomplete.PlacesAutocompleteTextView;
import com.seatgeek.placesautocomplete.model.Place;
import com.seatgeek.placesautocomplete.model.PlaceDetails;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddLocationActivity extends AppCompatActivity {
    private final String TAG = AddLocationActivity.class.getSimpleName();
    @BindView(R.id.clAddLocationActivityContainer)
    CoordinatorLayout clAddLocationActivityContainer;
    @BindView(R.id.ivAddPicture)
    ImageView ivAddPicture;
    @BindView(R.id.etLocationDescription)
    EditText etLocationDescription;
    @BindView(R.id.patvLocationAddress)
    PlacesAutocompleteTextView patvLocationAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);
        ButterKnife.bind(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        patvLocationAddress.setOnPlaceSelectedListener(
                new OnPlaceSelectedListener() {
                    @Override
                    public void onPlaceSelected(final Place place) {
                        patvLocationAddress.getDetailsFor(place, new DetailsCallback() {
                            @Override
                            public void onSuccess(PlaceDetails placeDetails) {
                                Log.v(TAG,"Longitude:" + placeDetails.geometry.location.lng);
                                Log.v(TAG,"Latitude:" + placeDetails.geometry.location.lat);
                            }

                            @Override
                            public void onFailure(Throwable throwable) {

                            }
                        });
                        // do something awesome with the selected place
                    }
                }
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_add_location_activity, menu);
        // Get the SearchView and set the searchable configuration
        MenuItem item = menu.findItem(R.id.action_print);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

}
