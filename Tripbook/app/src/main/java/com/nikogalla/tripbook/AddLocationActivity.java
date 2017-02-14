package com.nikogalla.tripbook;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.nikogalla.tripbook.data.FirebaseHelper;
import com.nikogalla.tripbook.models.Comment;
import com.nikogalla.tripbook.models.Location;
import com.nikogalla.tripbook.models.Photo;
import com.nikogalla.tripbook.models.Rate;
import com.nikogalla.tripbook.utils.DateUtils;
import com.nikogalla.tripbook.utils.StatusSnackBars;
import com.squareup.picasso.Picasso;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddLocationActivity extends AppCompatActivity implements OnConnectionFailedListener, Validator.ValidationListener {
    private final String TAG = AddLocationActivity.class.getSimpleName();
    private final String FIREBASE_BUCKET = "gs://tripbook-aa611.appspot.com/";
    static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    static final int REQUEST_SELECT_PHOTO = 2;
    static final int REQUEST_IMAGE_CAPTURE = 3;
    @BindView(R.id.tbAddlocation)
    Toolbar tbAddlocation;
    @BindView(R.id.clAddLocationActivityContainer)
    CoordinatorLayout clAddLocationActivityContainer;
    @BindView(R.id.ivAddPicture)
    ImageView ivAddPicture;
    @BindView(R.id.ivLocationName)
    ImageView ivLocationName;

    @NotEmpty
    @BindView(R.id.etLocationName)
    EditText etLocationName;

    @NotEmpty
    @BindView(R.id.etLocationAddress)
    EditText etLocationAddress;

    @NotEmpty
    @BindView(R.id.etLocationDescription)
    EditText etLocationDescription;
    @BindView(R.id.cpvUpload)
    CircularProgressView cpvUpload;

    String mCurrentPhotoPath;
    private DatabaseReference mDatabase;
    private Context mContext;
    private FirebaseStorage mStorage;
    StorageReference mStorageRef;
    StorageReference mImagesRef;
    MenuItem mConfirmMenuItem;
    private GoogleApiClient mGoogleApiClient;
    Place mPlace;
    Validator mValidator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);
        ButterKnife.bind(this);
        mContext = this;
        tbAddlocation.setTitle(getString(R.string.add_location));
        setSupportActionBar(tbAddlocation);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mDatabase = FirebaseHelper.getDatabase().getReference();
        mStorage = FirebaseStorage.getInstance();
        mStorageRef = mStorage.getReferenceFromUrl(FIREBASE_BUCKET);
        mImagesRef = mStorageRef.child("images");
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();
        mValidator = new Validator(this);
        mValidator.setValidationListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        etLocationName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    startSearchWithGoogleIntent();
                }
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
        etLocationName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSearchWithGoogleIntent();
            }
        });
    }

    public void startSearchWithGoogleIntent(){
        try {
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                    .build(AddLocationActivity.this).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra(getString(R.string.location_id),etLocationName.getText().toString());
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            StatusSnackBars.getErrorSnackBar(getString(R.string.unable_to_find_places),clAddLocationActivityContainer).show();
        } catch (GooglePlayServicesNotAvailableException e) {
            StatusSnackBars.getErrorSnackBar(getString(R.string.google_play_services_unavailable),clAddLocationActivityContainer).show();
        }
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
                mValidator.validate();
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
            }catch (Exception e){

            }
            // Log.d(TAG, String.valueOf(bitmap));
        }
        else if ((requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE)){
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                mPlace = place;
                etLocationName.setText(mPlace.getName());
                etLocationAddress.setText(mPlace.getAddress());
                Log.i(TAG, "Place: " + place.getName());
                Drawable curDrawable = ivAddPicture.getDrawable();
                if (mCurrentPhotoPath == null){
                   new GetPlacePhotos().execute(mPlace);
                }
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                StatusSnackBars.getErrorSnackBar(getString(R.string.unable_to_find_places) + ", status: " + status,clAddLocationActivityContainer).show();
                Log.i(TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    @Override
    public void onValidationSucceeded() {
        Log.v(TAG,"Validation succeded");
        hideKeyboard();
        checkIflocationWithSameNameExists();
    }

    public void checkIflocationWithSameNameExists(){
        Query query = mDatabase.child(Location.LOCATION_TABLE_NAME).orderByChild("name").equalTo(etLocationName.getText().toString());
        ValueEventListener valueEventListener = new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.getChildren().iterator().hasNext()){
                    Toast.makeText(mContext,getString(R.string.duplicate_location_name),Toast.LENGTH_SHORT).show();
                    Log.d(TAG,"Already exists");
                }else{
                    Log.d(TAG,"New location!");
                    uploadImageAndWriteLocation(mPlace.getAddress().toString(),String.valueOf(etLocationDescription.getText()),
                            mPlace.getLatLng().latitude,mPlace.getLatLng().longitude,mPlace.getName().toString());
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Toast.makeText(mContext,getString(R.string.database_error),Toast.LENGTH_SHORT).show();
            }
        };
        query.addListenerForSingleValueEvent(valueEventListener);
    }

    public void hideKeyboard(){
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(this);

            // Display error messages ;)
            if (view instanceof EditText) {
                ((EditText) view).setError(message);
            } else {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        }
    }

    private class GetPlacePhotos extends AsyncTask<Place, Integer, Bitmap> {
        protected Bitmap doInBackground(Place... places) {
            Place place = places[0];
            PlacePhotoMetadataResult result = Places.GeoDataApi.getPlacePhotos(mGoogleApiClient, place.getId()).await();
            if (result != null && result.getStatus().isSuccess()) {
                try{
                    PlacePhotoMetadataBuffer photoMetadataBuffer = result.getPhotoMetadata();
                    // Get the first photo in the list.
                    PlacePhotoMetadata photo = photoMetadataBuffer.get(0);
                    // Get a full-size bitmap for the photo.
                    Bitmap image = photo.getPhoto(mGoogleApiClient).await().getBitmap();
                    photoMetadataBuffer.release();
                    return image;
                }catch (Exception e){
                    Log.d(TAG,"Error retrieving picture for place :" + e.getMessage());
                    return null;
                }
            }
           return null;
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap!=null){
                ivAddPicture.setImageBitmap(bitmap);
                ivAddPicture.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }else{
                Bitmap icon = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.camera_placeholder);
                ivAddPicture.setImageBitmap(icon);
            }
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
    }

    private byte[] getCompressedBitmapByteArray(Bitmap origBitmap){
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        origBitmap.compress(Bitmap.CompressFormat.JPEG,45,out);
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
        String locationName = mPlace.getName().toString();
        locationName = locationName.replaceAll("[^a-zA-Z0-9.-]", "_");
        StorageReference locationRef = mImagesRef.child(locationName);
        // Get the data from an ImageView as bytes
        ivAddPicture.setDrawingCacheEnabled(true);
        ivAddPicture.buildDrawingCache();
        Bitmap bitmap = ivAddPicture.getDrawingCache();
        byte[] data = getCompressedBitmapByteArray(bitmap);
        UploadTask uploadTask = locationRef.putBytes(data);
        cpvUpload.setVisibility(View.VISIBLE);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                StatusSnackBars.getErrorSnackBar(getString(R.string.add_location_error),clAddLocationActivityContainer).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                String locationKey = mDatabase.child("locations").push().getKey();
                Location location = new Location(locationKey,address, latitude, longitude, name, description,userId);
                Date now = new Date();
                Photo photo = new Photo(downloadUrl.toString(), DateUtils.getUTCDateStringFromdate(now),userId);
                HashMap<String,Photo> photoHashMap = new HashMap<String, Photo>();
                photoHashMap.put(locationKey,photo);
                location.setPhotos(photoHashMap);
                HashMap<String,Comment> commentsHashMap = new HashMap<String, Comment>();
                location.setComments(commentsHashMap);
                HashMap<String,Rate> ratesHashMap = new HashMap<String, Rate>();
                location.setRates(ratesHashMap);

                Map<String, Object> locationValues = location.toMap();
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("/locations/" + locationKey, locationValues);
                mDatabase.updateChildren(childUpdates);
                cpvUpload.setVisibility(View.GONE);
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

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG,"Unable to initialize location api");
    }


}
