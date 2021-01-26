package com.tiberiugaspar.tpjadcontactsapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.tiberiugaspar.tpjadcontactsapp.utils.EncryptionUtils;
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
    /**
     * OnProfileImageClickListener - starts a image chooser activity provided by the system
     * where user can select a image from his/her device.
     */
    private final View.OnClickListener onProfileImageClickListener = view -> {

        //create a new intent object with the ACTION_GET_CONTENT flag
        // (used to retrieve content from external storage)
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);

        //set the content type to image/jpeg and start the activity for result
        intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(Intent.createChooser(intent,
                getString(R.string.select_app_to_continue)),
                REQ_CODE_PHOTO_PICKER);
    };

    private final List<PhoneNumber> phoneNumberList = new ArrayList<>();
    private PhoneNumberAdapter adapter;

    FirebaseFirestore db;

    private Contact contact = new Contact();

    private Uri selectedImageUri = null;

    private String userId = null;
    /**
     * OnPhoneNumberAddClickListener - adds a new PhoneNumber in the adapter's phoneNumberList
     * and notifies the adapter that a new item was inserted
     */
    private final View.OnClickListener onPhoneNumberAddClickListener = view -> {

        phoneNumberList.add(new PhoneNumber("", 0));
        adapter.notifyItemRangeInserted(phoneNumberList.size() - 1, phoneNumberList.size());
    };
    private TextInputEditText firstName, lastName, email;

    /**
     * initialize all java objects to their view correspondents
     */
    private void findViewsByIds() {
        addNumber = findViewById(R.id.image_add_number);
        addPhoto = findViewById(R.id.contact_image);
        firstName = findViewById(R.id.first_name);
        lastName = findViewById(R.id.last_name);
        email = findViewById(R.id.email);

        RecyclerView recyclerView = findViewById(R.id.recycler_phone_numbers);

        //initialize the adapter
        adapter = new PhoneNumberAdapter(phoneNumberList, this);

        //set the adapter to the recyclerView
        recyclerView.setAdapter(adapter);
        //set a new Linear layout manager to the recyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
    }

    /**
     * set onClickListeners for the ImageViews addPhoto (contact profile picture)
     * and addNumber (add a new entry in the PhoneNumber list)
     */
    private void initializeListeners() {
        addPhoto.setOnClickListener(onProfileImageClickListener);
        addNumber.setOnClickListener(onPhoneNumberAddClickListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //set the content according to the layout xml file
        setContentView(R.layout.activity_add_edit_contact);
        Toolbar toolbar = findViewById(R.id.toolbar);

        //initialize the floating action button for saving the contact and set onClickListener
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {

            if (fieldsAreValid()) {
                //if fields are valid we update/save the contact

                if (contact.getContactId() != null && contact.getContactId().length() > 1) {
                    //if the contact object contains an contactId, that means we are editing an
                    //existing contact, therefore we have to update its values in the database
                    updateContact();

                } else {
                    //if the contact doesn't have a contact id, it means we are adding a new contact,
                    //therefore we have to save it into the database
                    saveContact();
                }
            }
        });

        //initialize views and listeners
        findViewsByIds();
        initializeListeners();

        //get an instance of Firebase database
        db = FirebaseFirestore.getInstance();

        //get the intent that started the activity
        Intent intent = getIntent();

        if (intent.hasExtra(EXTRA_CONTACT_ID)) {

            //if the intent contains the EXTRA_CONTACT_ID, it means the activity is supposed to
            //handle the editing of an existing contact

            //set the title of toolbar to "Edit contact"
            toolbar.setTitle(R.string.edit_contact);

            //get the contactId from the intent using its getStringExtra method
            String contactId = intent.getStringExtra(EXTRA_CONTACT_ID);

            //get the reference of the contact by its id
            DocumentReference docRef = db.collection("contacts").document(contactId);

            //get the document from database and attach an onSuccessListener
            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {

                    //cast the DocumentSnapshot object to a Contact object
                    contact = documentSnapshot.toObject(Contact.class);

                    //decrypt the encrypted contact
                    contact = EncryptionUtils.decryptContact(contact);

                    if (Objects.requireNonNull(contact).getUriToImage() == null
                            || contact.getUriToImage().equals("")
                            || contact.getUriToImage().equals("null")) {

                        //if the imageUri is null or empty string, we set the contact profile picture
                        // to be its  initials using TextDrawable
                        TextDrawable drawable = TextDrawable.builder()
                                .beginConfig()
                                .width(150)
                                .height(150)
                                .endConfig()
                                .buildRound(getContactInitials(contact), getRandomColor());

                        addPhoto.setImageDrawable(drawable);

                    } else {

                        //if the imageUri is not null, we download the profile picture from Firebase
                        //Storage and we set it to the ImageView using Glide
                        Glide.with(addPhoto.getContext()).load(contact.getUriToImage())
                                .circleCrop().into(addPhoto);
                    }

                    //we set the other fields
                    firstName.setText(contact.getFirstName());
                    lastName.setText(contact.getLastName());
                    email.setText(contact.getEmail());
                    phoneNumberList.addAll(contact.getPhoneNumberList());

                    //and ultimately we notify the adapter that the list of phone numbers for this contact
                    //has changed and it should update the views
                    adapter.notifyDataSetChanged();
                }
            });

        } else {
            //if intent doesn't have the EXTRA value, it means the activity is meant to add a new user
            //and we initialize views with dummy values
            phoneNumberList.add(new PhoneNumber("", 0));
            adapter.notifyItemRangeInserted(0, phoneNumberList.size());

            userId = SharedPrefUtils.getUserId(getApplicationContext());
            toolbar.setTitle(R.string.new_contact);
        }

        setSupportActionBar(toolbar);

        //display the back button on the toolbar
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //add an onClickListener for the back button displayed in the toolbar to set the
        //activity result as RESULT_CANCELED and to finish the activity
        toolbar.setNavigationOnClickListener(view -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }

    /**
     * Used to set all the fields from the views to the actual instance of the contact
     */
    private void retrieveDataFromViews() {
        contact.setFirstName(Objects.requireNonNull(firstName.getText()).toString());
        contact.setLastName(Objects.requireNonNull(lastName.getText()).toString());
        contact.setPhoneNumberList(phoneNumberList);
        contact.setEmail(Objects.requireNonNull(email.getText()).toString());
        if (selectedImageUri != null) {
            contact.setUriToImage(String.valueOf(selectedImageUri));
        }
    }

    /**
     * Void method used to save the contact into the Firebase Firestore Database
     */
    private void saveContact() {

        //set the contact fields from inputs
        retrieveDataFromViews();
        contact.setUserId(userId);

        //create a new document reference in the database
        DocumentReference docRef = db.collection("contacts").document();

        //set the contact's id to be the same as its document reference id
        contact.setContactId(docRef.getId());

        //encrypt the contact
        contact = EncryptionUtils.encryptContact(contact);

        //insert it into the database and add onSuccessListener and onFailureListener
        docRef.set(contact).addOnSuccessListener(aVoid -> {
            //if the upload was successful, we display a Toast, set the result to be RESULT_OK
            //and we close the activity

            Toast.makeText(
                    AddEditContactActivity.this, R.string.contact_add_success,
                    Toast.LENGTH_SHORT).show();

            Log.i(TAG, "onSuccess: contact successfully inserted");

            setResult(RESULT_OK);
            finish();

        }).addOnFailureListener(e -> {

            //if the upload fails, we display a Toast containing an error message and we log the error
            Toast.makeText(
                    AddEditContactActivity.this, R.string.contact_error_adding,
                    Toast.LENGTH_SHORT).show();
            Log.e(TAG, "onFailure: " + e.getMessage());
        });
    }

    /**
     * Void method used to update an existing contact
     */
    private void updateContact() {

        //set the contact with values from the user input
        retrieveDataFromViews();

        //encrypt the contact
        contact = EncryptionUtils.encryptContact(contact);

        //update the contact fields in Database
        db.collection("contacts").document(contact.getContactId())
                .update("firstName", contact.getFirstName(),
                        "lastName", contact.getLastName(),
                        "email", contact.getEmail(),
                        "uriToImage", contact.getUriToImage(),
                        "phoneNumberList", contact.getPhoneNumberList())
                .addOnSuccessListener(aVoid -> {
                    //add onSuccessListener

                    //show a Toast with a success message
                    Toast.makeText(AddEditContactActivity.this, R.string.update_success,
                            Toast.LENGTH_SHORT).show();

                    //set the activity result to RESULT_OK and finish the activity
                    setResult(RESULT_OK);
                    finish();
                }).addOnFailureListener(e ->
                //add an onFailureListener and show a Toast with an error message
                Toast.makeText(AddEditContactActivity.this, R.string.try_again_error_message,
                        Toast.LENGTH_SHORT).show());

    }

    /**
     * Simple validation method
     *
     * @return true if the first name and at least one phone number were introduced; false otherwise.
     */
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

    /**
     * @param requestCode the request from which the activity initiated
     * @param resultCode  the result for the given request
     * @param data        any extra data transmitted via the intent object
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        //if requestCode is our REQ_CODE_PHOTO_PICKER and the result is OK, it means that
        // the user successfully selected a photo from the internal storage.

        // we upload this photo to our cloud storage using an uploadTask

        if (requestCode == REQ_CODE_PHOTO_PICKER && resultCode == RESULT_OK) {

            //get the path to the selected image from the data parameter
            selectedImageUri = data.getData();

            //instantiate a StorageReference object
            final StorageReference storageReference = FirebaseStorage.getInstance()
                    .getReference().child("contacts_photos")
                    .child(selectedImageUri.getLastPathSegment());

            //create the upload task
            UploadTask uploadTask = storageReference.putFile(selectedImageUri);

            //add another task after the upload is done
            uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    //if upload failed, throw the exepton
                    throw task.getException();
                }

                //else return the uploaded image's downloadUrl
                return storageReference.getDownloadUrl();
            }).addOnCompleteListener(task -> {

                //add another onCompleteListener
                if (task.isSuccessful()) {

                    //if task is successful, the task result will be the uploaded image's downloadUrl
                    // and we set it to the selected image uri
                    selectedImageUri = task.getResult();

                    //and we set the photo to our ImageView to be displayed as a circular image
                    Glide.with(addPhoto.getContext()).load(selectedImageUri).circleCrop().into(addPhoto);
                }
            });

        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}