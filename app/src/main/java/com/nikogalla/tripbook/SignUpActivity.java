package com.nikogalla.tripbook;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.ResultCodes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.nikogalla.tripbook.data.LocationDbHelper;
import com.nikogalla.tripbook.models.User;

import java.util.Arrays;

public class SignUpActivity extends AppCompatActivity {
    // Choose an arbitrary request code value
    private static final int RC_SIGN_IN = 123;
    private static final String TAG = SignUpActivity.class.getSimpleName();
    private Context mContext;
    // Global variables
    // A content resolver for accessing the provider
    ContentResolver mResolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_sign_up);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        mContext = this;
        if (auth.getCurrentUser() != null) {
            updateUserInfo(auth.getCurrentUser());
            // Turn on periodic sync
            // Get the content resolver for your app
            mResolver = getContentResolver();
            Intent intent = new Intent(mContext,AroundYouActivity.class);
            startActivity(intent);
        } else {
            // not signed in
            buildSignInIntent();
        }
    }

    public void buildSignInIntent(){
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setProviders(Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
                                new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build()))
                        .setTheme(R.style.Splash)
                        .setIsSmartLockEnabled(false)
                        .setLogo(R.drawable.tripbook_logo_splash)
                        .build(),
                RC_SIGN_IN);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // RC_SIGN_IN is the request code you passed into startActivityForResult(...) when starting the sign in flow.
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            // Successfully signed in
            if (resultCode == ResultCodes.OK) {
                //startActivity(SignedInActivity.createIntent(this, response));
                updateUserInfo(FirebaseAuth.getInstance().getCurrentUser());
                Intent intent = new Intent(mContext,AroundYouActivity.class);
                startActivity(intent);
                finish();
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    Toast.makeText(mContext,getString(R.string.sign_in_canceled),Toast.LENGTH_SHORT).show();
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(mContext,getString(R.string.no_network),Toast.LENGTH_SHORT).show();;
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    Toast.makeText(mContext,getString(R.string.unknown_error),Toast.LENGTH_SHORT).show();;
                    return;
                }
            }

            //showSnackbar(R.string.unknown_sign_in_response);
        }
    }

    public void updateUserInfo(FirebaseUser user){
        String uid = user.getUid();
        String name = user.getDisplayName();
        String email = user.getEmail();
        String provider = user.getProviderId();
        String pictureUrl;
        if (user.getPhotoUrl()!=null){
            pictureUrl = user.getPhotoUrl().toString();
        }else{
            pictureUrl = null;
        }
        User tripbookUser = new User(email,name,pictureUrl,provider,uid);
        LocationDbHelper.saveUserLocally(tripbookUser,mContext);
    }
}
