package com.tiberiugaspar.tpjadcontactsapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tiberiugaspar.tpjadcontactsapp.adapters.ContactAdapter;
import com.tiberiugaspar.tpjadcontactsapp.adapters.PhoneNumberAdapter;
import com.tiberiugaspar.tpjadcontactsapp.models.Contact;
import com.tiberiugaspar.tpjadcontactsapp.models.PhoneNumber;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
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

    private static final String TAG = "AddEditContactActivity";
    private ImageView addNumber, addPhoto;
    private TextInputEditText firstName, lastName, email, phoneNumber;
    private RecyclerView recyclerView;
    private PhoneNumberAdapter adapter;
    private List<PhoneNumber> phoneNumberList = new ArrayList<>();

    private Uri selectedImageUri;

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
                saveContact();
            }
        });

        findViewsByIds();
        initializeListeners();
    }

    private void saveContact(){
        Contact contact = new Contact();
        contact.setFirstName(Objects.requireNonNull(firstName.getText()).toString());
        contact.setLastName(Objects.requireNonNull(lastName.getText()).toString());
        contact.setPhoneNumberList(adapter.phoneNumberList);
        contact.setEmail(Objects.requireNonNull(email.getText()).toString());
        contact.setUriToImage(String.valueOf(selectedImageUri));


        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference docRef = db.collection("contacts").document();

        contact.setContactId(docRef.getId());

        docRef.set(contact).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(
                        AddEditContactActivity.this, R.string.contact_add_success,
                        Toast.LENGTH_SHORT).show();
                Log.i(TAG, "onSuccess: contact successfully inserted");
                setResult(RESULT_OK);
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(
                        AddEditContactActivity.this, R.string.contact_error_adding,
                        Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onFailure: " + e.getMessage());
            }
        });
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
        adapter.phoneNumberList.add(new PhoneNumber("",0));
        adapter.notifyItemRangeInserted(adapter.phoneNumberList.size()-1, adapter.phoneNumberList.size());
    };

    private boolean areFieldsValid(){
        Objects.requireNonNull(firstName.getText()).toString();
        if (firstName.getText().toString().equals("")){
            firstName.setError(getString(R.string.first_name_error));
            return false;
        }
        if (adapter.phoneNumberList.isEmpty()
                || adapter.phoneNumberList.get(0).getPhoneNumber() == null
                || adapter.phoneNumberList.get(0).getPhoneNumber().equals("")){
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
            selectedImageUri = data.getData();
            Glide.with(addPhoto.getContext()).load(selectedImageUri).into(addPhoto);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}