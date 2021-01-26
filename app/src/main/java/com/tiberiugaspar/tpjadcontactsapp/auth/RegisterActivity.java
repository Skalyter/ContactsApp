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

import static com.tiberiugaspar.tpjadcontactsapp.utils.TAGS.TIME_INTERVAL;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText email, password;

    /**
     * OnClickListener to be attached to the register {@link Button}
     *
     * <p>It checks whether the email and password are valid. If so, a new {@link FirebaseAuth} object is
     * instantiated and we try to register the new user.</p>
     */
    private final View.OnClickListener onRegisterClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (fieldsAreValid()) {
                //get username and password from user's input
                String username = email.getText().toString();
                String pass = password.getText().toString();

                //instantiate a new FirebaseAuth object
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                //send the createUserWithEmailAndPassword request to the authentication server
                //and attach an OnCompleteListener
                firebaseAuth.createUserWithEmailAndPassword(username, pass)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                //if task is successful it means that the user was successfully created
                                if (task.isSuccessful()) {

                                    //get the fresh registered user from the current instance
                                    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

                                    //assert that the firebaseUser is not null
                                    assert firebaseUser != null;

                                    //get its Id and store it in app's shared preferences
                                    String userId = firebaseUser.getUid();
                                    saveStringSharedPreference(userId);

                                    //start the MainActivity using a new Intent
                                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    //finish the current activity
                                    finish();
                                }else {
                                    //if task is not successful, that means that something bad happened with the connection
                                    //and the user may try again
                                    Toast.makeText(RegisterActivity.this, R.string.try_again_error_message,
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        }
    };
    /**
     * OnClickListener to be attached to the toLogin {@link TextView}
     *
     * <p>If the user already has an account but arrived in the {@link RegisterActivity}
     * by mistake, we are letting him/her to go back to the {@link LoginActivity}
     * by tapping on this TextView</p>
     */
    private final View.OnClickListener onLoginClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //instantiate a new Intent object from this context to the LoginActivity
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            //start the activity from intent
            startActivity(intent);
            //finish the current activity
            finish();
        }
    };
    /**
     * The timestamp for the last time the back button was pressed - saved as a long value
     */
    private long mBackPressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set the layout to be shown
        setContentView(R.layout.activity_register);

        //find the View objects from the layout
        email = findViewById(R.id.input_email);
        password = findViewById(R.id.input_password);
        Button register = findViewById(R.id.button_register);
        TextView toLogin = findViewById(R.id.to_login);

        //attack onClickListeners for the Register Button and toLogin TextView
        register.setOnClickListener(onRegisterClickListener);
        toLogin.setOnClickListener(onLoginClickListener);
    }

    /**
     * Overriding the onBackPressed() base method to handle user's navigation patterns
     *
     * <p>To eliminate any possibility of the user to get stuck between the {@link LoginActivity}
     * and {@link RegisterActivity} by switching multiple times from one to another, we destroy
     * each activity as soon as the user starts the other. If the user wants to go back to the
     * {@link LoginActivity}, it is very likely that he/she will press the back button.
     * This will cause the app to close, as there is no other Activity in the stack to be shown up,
     * which is not what the user intended.</p>
     * <p>In this scope, we overrode the onBackPressed() method to handle this behaviour.
     * If the user press the back button, we show him a {@link Toast}, to inform him/her to press
     * back again if he wants to exit the app. Otherwise, he can tap on the toLogin TextView to navigate
     * back to the {@link LoginActivity}.</p>
     */
    @Override
    public void onBackPressed() {
        //if last instance when the back button was pressed + the time interval is bigger than
        //System's current time in milliseconds, we call the normal behaviour of the back button
        //which closes the Activity
        if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
            super.onBackPressed();
            return;
        }
        //else we notify user to press again the back button to exit the app
        else {
            Toast.makeText(getBaseContext(), R.string.press_back_again, Toast.LENGTH_SHORT).show();
        }

        //we set the timestamp for the last time the back button was pressed
        mBackPressed = System.currentTimeMillis();
    }

    /**
     * A simple validation for the user inputs for email and password
     *
     * <p>It sets errors accordingly if any of the 2 fields are not valid.</p>
     *
     * @return true if fields are valid; false otherwise.
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