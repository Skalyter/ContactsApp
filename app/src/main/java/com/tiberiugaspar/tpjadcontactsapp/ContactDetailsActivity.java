package com.tiberiugaspar.tpjadcontactsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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

import static com.tiberiugaspar.tpjadcontactsapp.utils.TAGS.EXTRA_CONTACT_ID;
import static com.tiberiugaspar.tpjadcontactsapp.utils.TAGS.REQ_CODE_EDIT_CONTACT;

public class ContactDetailsActivity extends AppCompatActivity {

    private Contact contact;

    private String contactId;

    private ImageView contactImage;
    private TextView firstName, lastName, email;
    private RecyclerView recyclerView;
    private SimplePhoneNumberAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_details);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.contact_details);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> {
            setResult(RESULT_CANCELED);
            finish();
        });
        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_CONTACT_ID)){

            contactId = intent.getStringExtra(EXTRA_CONTACT_ID);

            if (contactId == null || contactId.equals("null") || contactId.equals("")){
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.contact_edit:
                //todo start activity addeditcontact
                Intent intent = new Intent(ContactDetailsActivity.this, AddEditContactActivity.class);
                intent.putExtra(EXTRA_CONTACT_ID, contact.getContactId());
                startActivityForResult(intent, REQ_CODE_EDIT_CONTACT);
                return true;
            case R.id.contact_delete:
                //todo delete contact and finish activity(+prompt message)
                return true;
            default:
                return  super.onOptionsItemSelected(item);
        }
    }

    private void getContactFromDb(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("contacts")
                .document(contactId);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                contact = documentSnapshot.toObject(Contact.class);
                initializeFields();
            }
        });
    }
    private void findViewsByIds(){
        contactImage = findViewById(R.id.contact_image);
        firstName = findViewById(R.id.contact_first_name);
        lastName = findViewById(R.id.contact_last_name);
        email = findViewById(R.id.contact_email);
        recyclerView = findViewById(R.id.recycler_contacts);
        List<PhoneNumber> list = new ArrayList<>();
        adapter = new SimplePhoneNumberAdapter(list, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
    }

    private void initializeFields() {
        Glide.with(contactImage.getContext()).load(contact.getUriToImage()).into(contactImage);
        firstName.setText(contact.getFirstName());
        lastName.setText(contact.getLastName());
        email.setText(contact.getEmail());
        adapter.phoneNumberList.clear();
        adapter.phoneNumberList.addAll(contact.getPhoneNumberList());
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQ_CODE_EDIT_CONTACT && resultCode == RESULT_OK){
            getContactFromDb();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}