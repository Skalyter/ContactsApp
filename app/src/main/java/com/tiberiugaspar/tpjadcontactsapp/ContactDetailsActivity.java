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

    private ImageView contactImage;
    private TextView firstName, lastName, email;
    private LinearLayout layoutEmail;
    private RecyclerView recyclerView;
    private SimplePhoneNumberAdapter adapter;

    private List<PhoneNumber> phoneNumberList = new ArrayList<>();

    private FirebaseFirestore db;

    private boolean contactEdited = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_details);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.contact_details);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> onBackPressed());
        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_CONTACT_ID)) {

            contactId = intent.getStringExtra(EXTRA_CONTACT_ID);

            if (contactId == null || contactId.equals("null") || contactId.equals("")) {
                Toast.makeText(this, R.string.try_again_error_message, Toast.LENGTH_SHORT).show();
                finish();
            }
            findViewsByIds();
            getContactFromDb();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.contact_details_menu, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (contactEdited) {
            setResult(RESULT_OK);
        } else {
            setResult(RESULT_CANCELED);
        }
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.contact_edit:
                Intent intent = new Intent(ContactDetailsActivity.this, AddEditContactActivity.class);
                intent.putExtra(EXTRA_CONTACT_ID, contact.getContactId());
                startActivityForResult(intent, REQ_CODE_EDIT_CONTACT);
                return true;
            case R.id.contact_delete:

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ContactDetailsActivity.this)
                        .setTitle(R.string.delete_contact_title)
                        .setMessage(R.string.delete_contact_message)
                        .setPositiveButton(getString(R.string.btn_delete), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                removeContactFromDb();
                            }
                        }).setNeutralButton(getString(R.string.btn_cancel), null);
                dialogBuilder.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void getContactFromDb() {
        db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("contacts")
                .document(contactId);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                contact = documentSnapshot.toObject(Contact.class);
                initializeViews();
            }
        });
    }

    private void findViewsByIds() {
        contactImage = findViewById(R.id.contact_image);
        firstName = findViewById(R.id.contact_first_name);
        lastName = findViewById(R.id.contact_last_name);
        email = findViewById(R.id.contact_email);
        layoutEmail = findViewById(R.id.layout_email);
        recyclerView = findViewById(R.id.recycler_contacts);
        adapter = new SimplePhoneNumberAdapter(phoneNumberList, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
    }

    private void initializeViews() {

        if (contact.getUriToImage() != null
                && !contact.getUriToImage().equals("")
                && !contact.getUriToImage().equals("null")) {
            Glide.with(contactImage.getContext()).load(contact.getUriToImage()).circleCrop().into(contactImage);
        } else {

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
        email.setText(contact.getEmail());

        phoneNumberList.clear();
        phoneNumberList.addAll(contact.getPhoneNumberList());
        adapter.notifyDataSetChanged();

        layoutEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Intent.ACTION_SENDTO);

                intent.setData(Uri.parse("mailto:" + contact.getEmail()));

                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQ_CODE_EDIT_CONTACT && resultCode == RESULT_OK) {
            getContactFromDb();
            contactEdited = true;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void removeContactFromDb() {
        db.collection("contacts")
                .document(contactId).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(ContactDetailsActivity.this, R.string.remove_contact_success_message,
                                Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ContactDetailsActivity.this, R.string.try_again_error_message,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}