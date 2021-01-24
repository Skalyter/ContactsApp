package com.tiberiugaspar.tpjadcontactsapp.auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tiberiugaspar.tpjadcontactsapp.MainActivity;
import com.tiberiugaspar.tpjadcontactsapp.R;
import com.tiberiugaspar.tpjadcontactsapp.utils.TAGS;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.input_email);
        password = findViewById(R.id.input_password);
        Button buttonLogin = findViewById(R.id.button_login);
        TextView toRegister = findViewById(R.id.to_register);

        buttonLogin.setOnClickListener(onLoginClickListener);
        toRegister.setOnClickListener(onRegisterClickListener);
    }

    private boolean fieldsAreValid(){

        if (!email.getText().toString().matches(TAGS.VALID_EMAIL_ADDRESS_REGEX)) {
            email.setError(getString(R.string.email_fill_in_warn));
            return false;
        }
        if (!(password.getText().toString().length() > 6)){
            password.setError(getString(R.string.password_warn));
            return false;
        }
        return true;
    }

    private final View.OnClickListener onLoginClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (fieldsAreValid()){
                String emailS = email.getText().toString();
                String passwordS = password.getText().toString();
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                firebaseAuth.signInWithEmailAndPassword(emailS, passwordS)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()){

                                    saveBooleanSharedPreference();

                                    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

                                    assert firebaseUser != null;

                                    String userId = firebaseUser.getUid();
                                    saveStringSharedPreference(userId);

                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();

                                } else {
                                    Toast.makeText(LoginActivity.this,
                                            R.string.try_again_error_message, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        }
    };

    private final View.OnClickListener onRegisterClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        }
    };

    private void saveBooleanSharedPreference() {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.key_logged_in), true);

        editor.apply();
    }

    private void saveStringSharedPreference(String userId){
        SharedPreferences sharedPreferences
                = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getString(R.string.key_user_id), userId);

        editor.apply();
    }
}