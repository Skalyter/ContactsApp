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

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(this, MainActivity.class));
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }

        finish();
    }
}