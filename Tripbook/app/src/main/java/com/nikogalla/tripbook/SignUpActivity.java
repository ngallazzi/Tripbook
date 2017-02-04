package com.nikogalla.tripbook;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.ResultCodes;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;

public class SignUpActivity extends AppCompatActivity {
    // Choose an arbitrary request code value
    private static final int RC_SIGN_IN = 123;
    private static final String TAG = SignUpActivity.class.getSimpleName();
    private Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_sign_up);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        mContext = this;
        if (auth.getCurrentUser() != null) {
            // already signed in
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
                Intent intent = new Intent(mContext,AroundYouActivity.class);
                startActivity(intent);
                finish();
                return;
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    Log.v(TAG,"Sign in canceled");
                    //showSnackbar(R.string.sign_in_cancelled);
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    //showSnackbar(R.string.no_internet_connection);
                    Log.v(TAG,"No internet connection");
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    //showSnackbar(R.string.unknown_error);
                    Log.v(TAG,"Unknown error");
                    return;
                }
            }

            //showSnackbar(R.string.unknown_sign_in_response);
        }
    }
}
