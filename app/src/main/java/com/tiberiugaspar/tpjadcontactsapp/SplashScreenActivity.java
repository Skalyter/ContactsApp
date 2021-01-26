package com.tiberiugaspar.tpjadcontactsapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.tiberiugaspar.tpjadcontactsapp.auth.LoginActivity;
import com.tiberiugaspar.tpjadcontactsapp.utils.EncryptionUtils;
import com.tiberiugaspar.tpjadcontactsapp.utils.SharedPrefUtils;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //generate public and private keys if the app is running for the first time
        String userId = SharedPrefUtils.getUserId(getApplicationContext());

        if (userId == null) {

            EncryptionUtils.generateKeys(getApplicationContext());
        }

        //get the instance of FirebaseAuth
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            //if currentUser is not null, we start the MainActivity,
            // as the user is already authenticated
            startActivity(new Intent(this, MainActivity.class));
        } else {

            //else we start the LoginActivity, as there is no user authenticated for this instance
            // of the app
            startActivity(new Intent(this, LoginActivity.class));
        }

        // lastly we finish the SplashScreenActivity because it served its purpose
        finish();
    }
}