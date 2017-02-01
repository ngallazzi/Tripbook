package com.nikogalla.tripbook;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nikogalla.tripbook.models.Location;
import com.nikogalla.tripbook.models.Photo;
import com.seatgeek.placesautocomplete.DetailsCallback;
import com.seatgeek.placesautocomplete.OnPlaceSelectedListener;
import com.seatgeek.placesautocomplete.PlacesAutocompleteTextView;
import com.seatgeek.placesautocomplete.model.Place;
import com.seatgeek.placesautocomplete.model.PlaceDetails;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddLocationActivity extends AppCompatActivity {
    private final String TAG = AddLocationActivity.class.getSimpleName();
    static final int REQUEST_TAKE_PHOTO = 1;
    @BindView(R.id.clAddLocationActivityContainer)
    CoordinatorLayout clAddLocationActivityContainer;
    @BindView(R.id.ivAddPicture)
    ImageView ivAddPicture;
    @BindView(R.id.etLocationName)
    EditText etLocationName;
    @BindView(R.id.etLocationDescription)
    EditText etLocationDescription;
    @BindView(R.id.patvLocationAddress)
    PlacesAutocompleteTextView patvLocationAddress;
    String mCurrentPhotoPath;
    private DatabaseReference mDatabase;
    private Double mLocationLatitude;
    private Double mLocationLongitude;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);
        ButterKnife.bind(this);
        mContext = this;
        mDatabase = FirebaseDatabase.getInstance().getReference();
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
                                mLocationLatitude = placeDetails.geometry.location.lat;
                                mLocationLongitude = placeDetails.geometry.location.lng;
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
        ivAddPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
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
                writeNewLocation(String.valueOf(patvLocationAddress.getText()),String.valueOf(etLocationDescription.getText()),
                        mLocationLatitude,mLocationLongitude,String.valueOf(etLocationName.getText()));
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }



    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.d(TAG,"Error occurred while creating the file");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.nikogalla.tripbook.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            setPic();
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void setPic() {
        // Get the dimensions of the View
        int targetW = ivAddPicture.getWidth();
        int targetH = ivAddPicture.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        ivAddPicture.setImageBitmap(bitmap);
        ivAddPicture.setScaleType(ImageView.ScaleType.CENTER_CROP);
    }

    private void writeNewLocation(String address, String description, Double latitude,Double longitude, String name) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String key = mDatabase.child("locations").push().getKey();
        Location location = new Location(address, latitude, longitude, name, description,userId);
        Map<String, Object> locationValues = location.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/locations/" + key, locationValues);
        mDatabase.updateChildren(childUpdates);
    }

    public void uploadImage(){

    }
}
