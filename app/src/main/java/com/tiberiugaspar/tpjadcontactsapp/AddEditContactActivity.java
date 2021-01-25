package com.tiberiugaspar.tpjadcontactsapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.tiberiugaspar.tpjadcontactsapp.adapters.PhoneNumberAdapter;
import com.tiberiugaspar.tpjadcontactsapp.models.Contact;
import com.tiberiugaspar.tpjadcontactsapp.models.PhoneNumber;
import com.tiberiugaspar.tpjadcontactsapp.utils.SharedPrefUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.tiberiugaspar.tpjadcontactsapp.utils.ContactUtils.getContactInitials;
import static com.tiberiugaspar.tpjadcontactsapp.utils.ContactUtils.getRandomColor;
import static com.tiberiugaspar.tpjadcontactsapp.utils.TAGS.EXTRA_CONTACT_ID;
import static com.tiberiugaspar.tpjadcontactsapp.utils.TAGS.REQ_CODE_PHOTO_PICKER;

public class AddEditContactActivity extends AppCompatActivity {

    private static final String TAG = "AddEditContactActivity";
    private ImageView addNumber, addPhoto;
    private final List<PhoneNumber> phoneNumberList = new ArrayList<>();
    private PhoneNumberAdapter adapter;
    private TextInputEditText firstName, lastName, email;

    FirebaseFirestore db;

    private Contact contact = new Contact();
    private Uri selectedImageUri = null;

    private String userId = null;

    private void findViewsByIds() {
        addNumber = findViewById(R.id.image_add_number);
        addPhoto = findViewById(R.id.contact_image);
        firstName = findViewById(R.id.first_name);
        lastName = findViewById(R.id.last_name);
        email = findViewById(R.id.email);

        RecyclerView recyclerView = findViewById(R.id.recycler_phone_numbers);
        adapter = new PhoneNumberAdapter(phoneNumberList, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
    }

    private void initializeListeners() {
        addPhoto.setOnClickListener(onProfileImageClickListener);
        addNumber.setOnClickListener(onPhoneNumberAddClickListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_contact);
        Toolbar toolbar = findViewById(R.id.toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            if (fieldsAreValid()) {
                if (contact.getContactId() != null && contact.getContactId().length() > 1) {
                    updateContact();
                } else {
                    saveContact();
                }
            }
        });

        findViewsByIds();
        initializeListeners();

        db = FirebaseFirestore.getInstance();

        Intent intent = getIntent();

        if (intent.hasExtra(EXTRA_CONTACT_ID)) {

            toolbar.setTitle(R.string.edit_contact);
            String contactId = intent.getStringExtra(EXTRA_CONTACT_ID);

            DocumentReference docRef = db.collection("contacts").document(contactId);

            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    contact = documentSnapshot.toObject(Contact.class);

                    if (Objects.requireNonNull(contact).getUriToImage() == null
                            || contact.getUriToImage().equals("")
                            || contact.getUriToImage().equals("null")) {

                        TextDrawable drawable = TextDrawable.builder()
                                .beginConfig()
                                .width(150)
                                .height(150)
                                .endConfig()
                                .buildRound(getContactInitials(contact), getRandomColor());

                        addPhoto.setImageDrawable(drawable);

                    } else {

                        Glide.with(addPhoto.getContext()).load(contact.getUriToImage())
                                .circleCrop().into(addPhoto);
                    }

                    firstName.setText(contact.getFirstName());
                    lastName.setText(contact.getLastName());
                    email.setText(contact.getEmail());
                    phoneNumberList.addAll(contact.getPhoneNumberList());

                    adapter.notifyDataSetChanged();
                }
            });

        } else {
            phoneNumberList.add(new PhoneNumber("", 0));
            adapter.notifyItemRangeInserted(0, phoneNumberList.size());

            userId = SharedPrefUtils.getUserId(getApplicationContext());
            toolbar.setTitle(R.string.new_contact);
        }

        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(view -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }

    private void retrieveDataFromViews() {
        contact.setFirstName(Objects.requireNonNull(firstName.getText()).toString());
        contact.setLastName(Objects.requireNonNull(lastName.getText()).toString());
        contact.setPhoneNumberList(phoneNumberList);
        contact.setEmail(Objects.requireNonNull(email.getText()).toString());
        if (selectedImageUri != null) {
            contact.setUriToImage(String.valueOf(selectedImageUri));
        }
    }

    private void saveContact() {

        retrieveDataFromViews();
        contact.setUserId(userId);

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

    private void updateContact() {

        retrieveDataFromViews();

        db.collection("contacts").document(contact.getContactId())
                .update("firstName", contact.getFirstName(),
                        "lastName", contact.getLastName(),
                        "email", contact.getEmail(),
                        "uriToImage", contact.getUriToImage(),
                        "phoneNumberList", contact.getPhoneNumberList())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(AddEditContactActivity.this, R.string.update_success,
                        Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AddEditContactActivity.this, R.string.try_again_error_message,
                        Toast.LENGTH_SHORT).show();
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
        phoneNumberList.add(new PhoneNumber("", 0));
        adapter.notifyItemRangeInserted(phoneNumberList.size() - 1, phoneNumberList.size());
    };

    private boolean fieldsAreValid() {
        Objects.requireNonNull(firstName.getText()).toString();
        if (firstName.getText().toString().equals("")) {
            firstName.setError(getString(R.string.first_name_error));
            return false;
        }
        if (phoneNumberList.isEmpty()
                || phoneNumberList.get(0).getPhoneNumber() == null
                || phoneNumberList.get(0).getPhoneNumber().equals("")) {
            Toast.makeText(this,
                    getString(R.string.phone_number_error),
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == REQ_CODE_PHOTO_PICKER && resultCode == RESULT_OK) {
            selectedImageUri = data.getData();
            final StorageReference storageReference = FirebaseStorage.getInstance()
                    .getReference().child("contacts_photos")
                    .child(selectedImageUri.getLastPathSegment());
            UploadTask uploadTask = storageReference.putFile(selectedImageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()){
                        throw task.getException();
                    }
                    return storageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){

                        selectedImageUri = task.getResult();

                        Glide.with(addPhoto.getContext()).load(selectedImageUri).circleCrop().into(addPhoto);
                    }
                }
            });

        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}