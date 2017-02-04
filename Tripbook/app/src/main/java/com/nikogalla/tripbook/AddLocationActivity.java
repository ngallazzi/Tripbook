package com.nikogalla.tripbook;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;

import com.facebook.internal.Utility;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nikogalla.tripbook.models.Location;
import com.nikogalla.tripbook.models.Photo;
import com.seatgeek.placesautocomplete.DetailsCallback;
import com.seatgeek.placesautocomplete.OnPlaceSelectedListener;
import com.seatgeek.placesautocomplete.PlacesAutocompleteTextView;
import com.seatgeek.placesautocomplete.model.Place;
import com.seatgeek.placesautocomplete.model.PlaceDetails;
import com.squareup.picasso.Picasso;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnTextChanged;

import static android.support.design.widget.Snackbar.Callback.DISMISS_EVENT_ACTION;

public class AddLocationActivity extends AppCompatActivity {
    private final String TAG = AddLocationActivity.class.getSimpleName();
    private final String FIREBASE_BUCKET = "gs://tripbook-aa611.appspot.com/";
    static final int REQUEST_SELECT_PHOTO = 1;
    static final int REQUEST_IMAGE_CAPTURE = 2;
    @BindView(R.id.tbAddlocation)
    Toolbar tbAddlocation;
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
    private boolean mPictureIsSet,mLocationNameIsSet,mLocationCoordinatesAreSet,mLocationDescriptionIsSet;
    private Context mContext;
    private FirebaseStorage mStorage;
    StorageReference mStorageRef;
    StorageReference mImagesRef;
    MenuItem mConfirmMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);
        ButterKnife.bind(this);
        mContext = this;
        tbAddlocation.setTitle(getString(R.string.add_location));
        setSupportActionBar(tbAddlocation);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance();
        mStorageRef = mStorage.getReferenceFromUrl(FIREBASE_BUCKET);
        mImagesRef = mStorageRef.child("images");
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
                                mLocationCoordinatesAreSet = true;
                                if (isLocationInsertComplete()){
                                    mConfirmMenuItem.setEnabled(true);
                                }
                            }

                            @Override
                            public void onFailure(Throwable throwable) {
                                mLocationCoordinatesAreSet = false;
                            }
                        });
                        // do something awesome with the selected place
                    }
                }
        );
        patvLocationAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                mLocationCoordinatesAreSet = false;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        etLocationName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                mLocationNameIsSet = false;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count>0){
                    mLocationNameIsSet = true;
                    if (isLocationInsertComplete()){
                        mConfirmMenuItem.setEnabled(true);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        etLocationDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                mLocationDescriptionIsSet = false;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count>0){
                    mLocationDescriptionIsSet = true;
                    if (isLocationInsertComplete()){
                        mConfirmMenuItem.setEnabled(true);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        ivAddPicture.setOnClickListener(new View.OnClickListener() {
            final CharSequence[] items = { getString(R.string.take_photo), getString(R.string.select_from_gallery), getString(R.string.cancel)};
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AddLocationActivity.this);
                builder.setTitle(getString(R.string.choose_location_photo));
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (items[item].equals(getString(R.string.take_photo))) {
                            dispatchTakePictureIntent();
                        } else if (items[item].equals(getString(R.string.select_from_gallery))) {
                            dispatchPictureFromGalleryIntent();
                        } else if (items[item].equals(getString(R.string.cancel))) {
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();
            }
        });
    }


    private boolean isLocationInsertComplete(){
        if (mPictureIsSet && mLocationNameIsSet && mLocationCoordinatesAreSet && mLocationDescriptionIsSet){
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_location, menu);
        // Get the SearchView and set the searchable configuration
        mConfirmMenuItem = menu.findItem(R.id.action_confirm_location);
        mConfirmMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                uploadImageAndWriteLocation(String.valueOf(patvLocationAddress.getText()),String.valueOf(etLocationDescription.getText()),
                        mLocationLatitude,mLocationLongitude,String.valueOf(etLocationName.getText()));
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private void dispatchPictureFromGalleryIntent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent,REQUEST_SELECT_PHOTO);
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
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.nikogalla.tripbook.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK)) {
            setPic();
        }
        else if ((requestCode == REQUEST_SELECT_PHOTO && resultCode == RESULT_OK)){
            Uri selectedImage = data.getData();
            try{
                Picasso.with(mContext).load(selectedImage).into(ivAddPicture);
                ivAddPicture.setScaleType(ImageView.ScaleType.CENTER_CROP);
                mPictureIsSet = true;
                if (isLocationInsertComplete()){
                    mConfirmMenuItem.setEnabled(true);
                }
            }catch (Exception e){

            }
            // Log.d(TAG, String.valueOf(bitmap));
        }
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
        mPictureIsSet = true;
        if (isLocationInsertComplete()){
            mConfirmMenuItem.setEnabled(true);
        }
    }

    private byte[] getCompressedBitmapByteArray(Bitmap origBitmap){
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        origBitmap.compress(Bitmap.CompressFormat.JPEG,75,out);
        Log.v(TAG,"Bitmap size: " + out.size());
        return out.toByteArray();
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

    public void uploadImageAndWriteLocation(final String address, final String description, final Double latitude, final Double longitude, final String name){
        // Create a reference to "mountains.jpg"
        String locationName = etLocationName.getText().toString();
        locationName = locationName.replaceAll("[^a-zA-Z0-9.-]", "_");
        StorageReference locationRef = mImagesRef.child(locationName);
        // Get the data from an ImageView as bytes
        ivAddPicture.setDrawingCacheEnabled(true);
        ivAddPicture.buildDrawingCache();
        Bitmap bitmap = ivAddPicture.getDrawingCache();
        byte[] data = getCompressedBitmapByteArray(bitmap);
        UploadTask uploadTask = locationRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                String locationKey = mDatabase.child("locations").push().getKey();
                Location location = new Location(address, latitude, longitude, name, description,userId);
                Date now = new Date();
                Photo photo = new Photo(downloadUrl.toString(),DateUtils.getUTCDateStringFromdate(now),userId);
                HashMap<String,Photo> photoHashMap = new HashMap<String, Photo>();
                photoHashMap.put(locationKey,photo);
                location.setPhotos(photoHashMap);
                Map<String, Object> locationValues = location.toMap();
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("/locations/" + locationKey, locationValues);
                mDatabase.updateChildren(childUpdates);
                Snackbar snackbar = StatusSnackBars.getStatusSnackBar(getString(R.string.add_location_successful),clAddLocationActivityContainer);
                snackbar.addCallback(new Snackbar.Callback(){
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        super.onDismissed(transientBottomBar, event);
                        finish();
                    }
                });
                snackbar.show();
            }
        });
    }
}
