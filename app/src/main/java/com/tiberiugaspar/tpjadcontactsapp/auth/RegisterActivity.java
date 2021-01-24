package com.tiberiugaspar.tpjadcontactsapp.auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText email, password;

    private static final int TIME_INTERVAL = 2000;
    private long mBackPressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        email = findViewById(R.id.input_email);
        password = findViewById(R.id.input_password);
        Button register = findViewById(R.id.button_register);
        TextView toLogin = findViewById(R.id.to_login);

        register.setOnClickListener(onRegisterClickListener);
        toLogin.setOnClickListener(onLoginClickListener);
    }
    private final View.OnClickListener onRegisterClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (fieldsAreValid()){
                String username = email.getText().toString();
                String pass = password.getText().toString();
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                firebaseAuth.createUserWithEmailAndPassword(username, pass)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()){
                                    saveBooleanSharedPreference();

                                    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

                                    assert firebaseUser != null;

                                    String userId = firebaseUser.getUid();
                                    saveStringSharedPreference(userId);

                                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }else {
                                    Toast.makeText(RegisterActivity.this, R.string.try_again_error_message,
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        }
    };

    private final View.OnClickListener onLoginClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    };

    @Override
    public void onBackPressed()
    {
        if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis())
        {
            super.onBackPressed();
            return;
        }
        else { Toast.makeText(getBaseContext(), R.string.press_back_again, Toast.LENGTH_SHORT).show(); }

        mBackPressed = System.currentTimeMillis();
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