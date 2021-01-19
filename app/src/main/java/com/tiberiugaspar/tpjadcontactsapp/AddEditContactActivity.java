package com.tiberiugaspar.tpjadcontactsapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.tiberiugaspar.tpjadcontactsapp.adapters.PhoneNumberAdapter;
import com.tiberiugaspar.tpjadcontactsapp.models.PhoneNumber;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.tiberiugaspar.tpjadcontactsapp.utils.TAGS.EXTRA_CONTACT_ID;
import static com.tiberiugaspar.tpjadcontactsapp.utils.TAGS.REQ_CODE_PHOTO_PICKER;

public class AddEditContactActivity extends AppCompatActivity {

    private ImageView addNumber, addPhoto;
    private TextInputEditText firstName, lastName, email, phoneNumber;
    private RecyclerView recyclerView;
    private PhoneNumberAdapter adapter;
    private List<PhoneNumber> phoneNumberList = new ArrayList<>();

    private void findViewsByIds(){
        addNumber = findViewById(R.id.image_add_number);
        addPhoto = findViewById(R.id.contact_image);
        firstName = findViewById(R.id.first_name);
        lastName = findViewById(R.id.last_name);
        email = findViewById(R.id.email);
        phoneNumber = findViewById(R.id.phone_number);

        recyclerView = findViewById(R.id.recycler_phone_numbers);
        adapter = new PhoneNumberAdapter(phoneNumberList, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
    }

    private void initializeListeners(){
        addPhoto.setOnClickListener(onProfileImageClickListener);
        addNumber.setOnClickListener(onPhoneNumberAddClickListener);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_contact);
        Toolbar toolbar = findViewById(R.id.toolbar);

        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_CONTACT_ID)){
            String contactId = intent.getStringExtra(EXTRA_CONTACT_ID);
            //TODO: populam field urile din BD
        } else {
            //TODO: initializam totul normal
            //no-op yet
        }

        toolbar.setTitle(R.string.new_contact);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            if (areFieldsValid()){
                //TODO: save contact in db
                Toast.makeText(AddEditContactActivity.this,
                        R.string.success_save,
                        Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }
        });

        findViewsByIds();
        initializeListeners();
    }

    private final View.OnClickListener onProfileImageClickListener = view -> {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(Intent.createChooser(intent,
                getString(R.string.select_app_to_continue)),
                REQ_CODE_PHOTO_PICKER);
    };

    private final View.OnClickListener onPhoneNumberAddClickListener = view -> {
        phoneNumberList.add(new PhoneNumber("", 0));
        adapter.notifyItemRangeInserted(phoneNumberList.size()-1, phoneNumberList.size());
    };

    private boolean areFieldsValid(){
        Objects.requireNonNull(firstName.getText()).toString();
        if (firstName.getText().toString().equals("")){
            firstName.setError(getString(R.string.first_name_error));
            return false;
        }
        if (phoneNumberList.isEmpty()
                || phoneNumberList.get(0).getPhoneNumber() == null
                || phoneNumberList.get(0).getPhoneNumber().equals("")){
            Toast.makeText(this,
                    getString(R.string.phone_number_error),
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == REQ_CODE_PHOTO_PICKER && resultCode == RESULT_OK){
            final Uri selectedImageUri = data.getData();
            Glide.with(addPhoto.getContext()).load(selectedImageUri).into(addPhoto);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}