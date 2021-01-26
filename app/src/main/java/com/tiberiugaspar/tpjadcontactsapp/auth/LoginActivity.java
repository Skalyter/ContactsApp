package com.tiberiugaspar.tpjadcontactsapp.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tiberiugaspar.tpjadcontactsapp.MainActivity;
import com.tiberiugaspar.tpjadcontactsapp.R;
import com.tiberiugaspar.tpjadcontactsapp.utils.TAGS;

/**
 * The activity which holds the login template and functionality
 * Extends the base {@link AppCompatActivity} class
 */
public class LoginActivity extends AppCompatActivity {

    private TextInputEditText email, password;

    /**
     * OnClickListener to be attached to the Login button
     *
     * <p>It is instantiated using the default constructor of the {@link View.OnClickListener}
     * interface, which overrides the onClick(View view) method. It checks if the fields are valid.
     * If so, the email and password are stored in String objects and we create a {@link FirebaseAuth}
     * instance for which we call the signInWitEmailAndPassword method and we attach a new
     * OnCompleteListener which overrides the onComplete() method. If the login task is successful,
     * we request the current user, for which we store the userId in shared preferences, for further
     * database requests and we start the {@link MainActivity}</p>
     */
    private final View.OnClickListener onLoginClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (fieldsAreValid()) {

                String emailS = email.getText().toString();
                String passwordS = password.getText().toString();

                //creating a FirebaseAuth instance
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

                //trying to sign in the user with his/her credentials
                firebaseAuth.signInWithEmailAndPassword(emailS, passwordS)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                //if task is successful, it means the login was successful, therefore
                                //we can go further in our application
                                if (task.isSuccessful()) {

                                    //get the current authenticated user
                                    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

                                    //assert that the current user is not null
                                    assert firebaseUser != null;

                                    //get the id of the current user and save it in the app's shared preferences
                                    String userId = firebaseUser.getUid();
                                    saveStringSharedPreference(userId);

                                    //instantiate a new Intent from the current context to the MainActivity
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    //start the activity
                                    startActivity(intent);
                                    //finish the LoginActivity
                                    finish();

                                } else {
                                    //if task is not successful, that means whether the email and password combination
                                    //is invalid, whether something strange happened to the connection
                                    Toast.makeText(LoginActivity.this,
                                            R.string.try_again_error_message, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        }
    };
    /**
     * OnClickListener attached to the toRegister TextView
     *
     * <p>When the user taps over the TextView, we want to send him/her to the RegisterActivity,
     * to allow them to create a new account.</p>
     */
    private final View.OnClickListener onRegisterClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //instantiate a new intent from this context to the RegisterActivity
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            //start the RegisterActivity
            startActivity(intent);
            //finish the current activity
            finish();
        }
    };

    /**
     * Called when the activity is created
     *
     * @param savedInstanceState used to retrieve information from previous instances (if any)
     *                           //no-op in this implementation
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setting the content according to the layout resource file activity_login.xml
        setContentView(R.layout.activity_login);

        //find all View objects from the layout and bind them to their java objects
        email = findViewById(R.id.input_email);
        password = findViewById(R.id.input_password);
        Button buttonLogin = findViewById(R.id.button_login);
        TextView toRegister = findViewById(R.id.to_register);

        //attach onClickListeners for the Login Button and toRegister TextView
        buttonLogin.setOnClickListener(onLoginClickListener);
        toRegister.setOnClickListener(onRegisterClickListener);
    }

    /**
     * A simple validation method for the user inputs for email and password
     *
     * <p>It checks whether the email matches the email regex from {@link TAGS}'s constant
     * VALID_EMAIL_ADDRESS_REGEX and if the password is at least 6 characters long.
     * If any of these conditions fails, their corresponding input field is set as invalid
     * and an error message is shown.</p>
     *
     * @return true if the fields are valid; false otherwise.
     */
    private boolean fieldsAreValid() {

        if (!(email.getText().toString().matches(TAGS.VALID_EMAIL_ADDRESS_REGEX))) {
            email.setError(getString(R.string.email_fill_in_warn));
            return false;
        }
        if (!(password.getText().toString().length() > 6)) {
            password.setError(getString(R.string.password_warn));
            return false;
        }
        return true;
    }

    /**
     * Used to save the userId into the shared preferences
     *
     * @param userId the Id of the current user retrieved from the {@link FirebaseAuth} instance
     */
    private void saveStringSharedPreference(String userId) {
        //get the sharedPreferences object for the application-level context
        SharedPreferences sharedPreferences
                = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        //instantiate an Editor object
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //put the user id value with its corresponding key saved as a resource in strings.xml key_user_id
        editor.putString(getString(R.string.key_user_id), userId);
        //apply changes in the shared preferences and auto close the editor
        editor.apply();
    }
}