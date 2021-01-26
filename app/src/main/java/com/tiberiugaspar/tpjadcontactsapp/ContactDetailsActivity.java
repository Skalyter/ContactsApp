package com.tiberiugaspar.tpjadcontactsapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tiberiugaspar.tpjadcontactsapp.adapters.SimplePhoneNumberAdapter;
import com.tiberiugaspar.tpjadcontactsapp.models.Contact;
import com.tiberiugaspar.tpjadcontactsapp.models.PhoneNumber;
import com.tiberiugaspar.tpjadcontactsapp.utils.EncryptionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.tiberiugaspar.tpjadcontactsapp.utils.ContactUtils.getContactInitials;
import static com.tiberiugaspar.tpjadcontactsapp.utils.ContactUtils.getRandomColor;
import static com.tiberiugaspar.tpjadcontactsapp.utils.TAGS.EXTRA_CONTACT_ID;
import static com.tiberiugaspar.tpjadcontactsapp.utils.TAGS.REQ_CODE_EDIT_CONTACT;

public class ContactDetailsActivity extends AppCompatActivity {

    private Contact contact;

    private String contactId;

    private ImageView contactImage, iconMail;
    private TextView firstName, lastName, email;
    private LinearLayout layoutEmail;

    private SimplePhoneNumberAdapter adapter;

    private final List<PhoneNumber> phoneNumberList = new ArrayList<>();

    private FirebaseFirestore db;

    private boolean contactEdited = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //set the layout from the xml resource file
        setContentView(R.layout.activity_contact_details);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.contact_details);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> onBackPressed());

        //get the intent from which the activity started
        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_CONTACT_ID)) {
            //if the intent has the EXTRA_CONTACT_ID we instantiate the view objects
            // and we get the contact from the db
            contactId = intent.getStringExtra(EXTRA_CONTACT_ID);

            //if the contactId is null or incorrect, we exit from the activity, as this should not happen
            if (contactId == null || contactId.equals("null") || contactId.equals("")) {
                Toast.makeText(this, R.string.try_again_error_message, Toast.LENGTH_SHORT).show();
                finish();
            }
            //we initialize the views and we get the contact from db
            findViewsByIds();
            getContactFromDb();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //we inflate the menu resource file
        getMenuInflater().inflate(R.menu.contact_details_menu, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        //if the contact was edited after entering in this activity, we set the result to
        // RESULT_OK in order for the MainActivity to update its layout with the updated value
        // of the edited contact
        if (contactEdited) {
            setResult(RESULT_OK);
        } else {
            //else we set the result to RESULT_CANCELED
            setResult(RESULT_CANCELED);
        }
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        //switch the id of the item selected from the menu: either the contact_edit button,
        // either the contact_delete button
        switch (item.getItemId()) {
            case R.id.contact_edit:

                //if user pressed the edit button, we start the AddEditActivity by passing the contact id
                Intent intent = new Intent(ContactDetailsActivity.this,
                        AddEditContactActivity.class);
                intent.putExtra(EXTRA_CONTACT_ID, contact.getContactId());
                startActivityForResult(intent, REQ_CODE_EDIT_CONTACT);
                return true;

            case R.id.contact_delete:

                //if user pressed the delete button, we show a Dialog to ask the user if he wants
                // to delete the contact, giving him 2 options: Delete and Cancel
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ContactDetailsActivity.this)
                        .setTitle(R.string.delete_contact_title)
                        .setMessage(R.string.delete_contact_message)
                        .setPositiveButton(getString(R.string.btn_delete), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //if the user taps on Delete, we remove the current contact from db
                                // and the activity automatically manages to close the current activity
                                // and to update the contact list from MainActivity
                                removeContactFromDb();
                            }
                            //if the user press on Cancel, we do nothing, just close the dialog
                        }).setNeutralButton(getString(R.string.btn_cancel), null);

                //with this method the dialog is shown
                dialogBuilder.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Void method to get the contact from the Firestore
     */
    private void getContactFromDb() {
        //getting the FirebaseFirestore instance
        db = FirebaseFirestore.getInstance();

        //creating a document reference with the current contactId
        DocumentReference docRef = db.collection("contacts")
                .document(contactId);

        //get the document reference
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                //add onSuccessListener and cast the documentSnapshot object to a Contact object
                contact = documentSnapshot.toObject(Contact.class);

                //decrypt the contact
                contact = EncryptionUtils.decryptContact(contact);

                //initialize views once the contact is retrieved from database
                initializeViews();
            }
        });
    }

    private void findViewsByIds() {

        //find all views by their ids
        contactImage = findViewById(R.id.contact_image);
        firstName = findViewById(R.id.contact_first_name);
        lastName = findViewById(R.id.contact_last_name);
        email = findViewById(R.id.contact_email);
        iconMail = findViewById(R.id.icon_mail);
        layoutEmail = findViewById(R.id.layout_email);
        RecyclerView recyclerView = findViewById(R.id.recycler_contacts);

        //initialize the adapter
        adapter = new SimplePhoneNumberAdapter(phoneNumberList, this);
        //set the adapter to recyclerView
        recyclerView.setAdapter(adapter);
        //set a linear layout manager to the recycler view
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
    }

    private void initializeViews() {

        //populate views with contact's actual data retrieved from database

        //if contact has a profile image, we display it using Glide
        if (contact.getUriToImage() != null
                && !contact.getUriToImage().equals("")
                && !contact.getUriToImage().equals("null")) {

            Glide.with(contactImage.getContext()).load(contact.getUriToImage()).circleCrop().into(contactImage);
        } else {

            //otherwise we use the TextDrawable to show its initials instead
            TextDrawable drawable = TextDrawable.builder()
                    .beginConfig()
                    .width(150)
                    .height(150)
                    .endConfig()
                    .buildRound(getContactInitials(contact), getRandomColor());

            contactImage.setImageDrawable(drawable);
        }

        firstName.setText(contact.getFirstName());
        lastName.setText(contact.getLastName());

        if (contact.getEmail() == null
                || contact.getEmail().equals("")
                || contact.getEmail().equals("null")) {

            //if contact has no email, we hide the email icon
            iconMail.setVisibility(View.GONE);

        } else {

            //else we attach an onClickListener to handle the generation of an intent
            // to start an Email app with the contact's email address
            email.setText(contact.getEmail());

            layoutEmail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //setting the intent action with ACTION_SENDTO flag - used for email
                    Intent intent = new Intent(Intent.ACTION_SENDTO);

                    //setting intent data mailto: to autocomplete the recipient
                    intent.setData(Uri.parse("mailto:" + contact.getEmail()));
                    //start the activity
                    startActivity(intent);
                }
            });
        }

        //clear the phoneNumber list if there is any residual items
        phoneNumberList.clear();
        //add all phone numbers from contact
        phoneNumberList.addAll(contact.getPhoneNumberList());
        //notify adapter that the dataset has changed
        adapter.notifyDataSetChanged();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //if the contact was edited, we set contactEdited variable to true
        // in order to propagate the RESULT_OK from AddEditContactActivity to the MainActivity
        // on onBackPressed
        if (requestCode == REQ_CODE_EDIT_CONTACT && resultCode == RESULT_OK) {
            getContactFromDb();
            contactEdited = true;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void removeContactFromDb() {

        //delete the contact from database by contactId
        db.collection("contacts")
                .document(contactId).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    //add onSuccessListener
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(ContactDetailsActivity.this, R.string.remove_contact_success_message,
                                Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            //add onFailureListener
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ContactDetailsActivity.this, R.string.try_again_error_message,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}