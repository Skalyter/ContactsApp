package com.tiberiugaspar.tpjadcontactsapp;

import android.content.Intent;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.tiberiugaspar.tpjadcontactsapp.adapters.ContactAdapter;
import com.tiberiugaspar.tpjadcontactsapp.auth.LoginActivity;
import com.tiberiugaspar.tpjadcontactsapp.models.Contact;
import com.tiberiugaspar.tpjadcontactsapp.utils.EncryptionV2;
import com.tiberiugaspar.tpjadcontactsapp.utils.SharedPrefUtils;
import com.tiberiugaspar.tpjadcontactsapp.utils.SwipeController;
import com.tiberiugaspar.tpjadcontactsapp.utils.SwipeControllerActions;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.tiberiugaspar.tpjadcontactsapp.utils.TAGS.REQ_CODE_ADD_CONTACT;
import static com.tiberiugaspar.tpjadcontactsapp.utils.TAGS.REQ_CODE_EDIT_CONTACT;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private final List<Contact> contactList = new ArrayList<>();
    private final List<Contact> temporalList = new ArrayList<>();
    private ContactAdapter adapter;

    private FirebaseFirestore db;

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.contacts);
        setSupportActionBar(toolbar);
        db = FirebaseFirestore.getInstance();

        initializeViews();
    }

    private void initializeViews() {

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddEditContactActivity.class);
                startActivityForResult(intent, REQ_CODE_ADD_CONTACT);
            }
        });

        RecyclerView recyclerView = findViewById(R.id.recycler_contacts);

        adapter = new ContactAdapter(MainActivity.this, contactList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(
                new LinearLayoutManager(MainActivity.this,
                        RecyclerView.VERTICAL,
                        false));
        userId = SharedPrefUtils.getUserId(getApplicationContext());
        getContactsFromDb();

        SwipeController swipeController = new SwipeController(this, new SwipeControllerActions() {
            @Override
            public void onLeftClicked(int position) {

                String phoneNumber = contactList.get(position)
                        .getPhoneNumberList()
                        .get(0).getPhoneNumber();

                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse(String.format("tel:%s", phoneNumber)));
                startActivity(callIntent);
            }

            @Override
            public void onRightClicked(int position) {

                String phoneNumber = contactList.get(position)
                        .getPhoneNumberList()
                        .get(0).getPhoneNumber();

                Intent smsIntent = new Intent(Intent.ACTION_VIEW);

                smsIntent.setData(Uri.parse(String.format("sms:%s", phoneNumber)));
                startActivity(smsIntent);
            }
        });
        ItemTouchHelper touchHelper = new ItemTouchHelper(swipeController);
        touchHelper.attachToRecyclerView(recyclerView);
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, @NonNull RecyclerView.State state) {
                swipeController.onDraw(c);
            }
        });
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (newText == null || newText.length() < 1) {
            contactList.clear();
            contactList.addAll(temporalList);
            adapter.notifyDataSetChanged();
        } else {
            getContactsForInput(newText);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.search_bar);

        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getString(R.string.search_contacts));
        searchView.setOnQueryTextListener(this);
        searchView.setIconified(true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.button_logout) {

            FirebaseAuth auth = FirebaseAuth.getInstance();
            auth.signOut();

            startActivity(new Intent(MainActivity.this, LoginActivity.class));

            finish();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQ_CODE_ADD_CONTACT && resultCode == RESULT_OK) {

            getContactsFromDb();
        }
        if (requestCode == REQ_CODE_EDIT_CONTACT && resultCode == RESULT_OK) {

            getContactsFromDb();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void getContactsFromDb() {
        db.collection("contacts")
                .whereEqualTo("userId", userId)
                .orderBy("firstName", Query.Direction.ASCENDING)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                contactList.clear();
                for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                    Contact contact = snapshot.toObject(Contact.class);
                    contactList.add(EncryptionV2.decryptContact(contact));
                }
                adapter.notifyDataSetChanged();
                temporalList.addAll(contactList);
            }
        });
    }

    private void getContactsForInput(String input) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            contactList.clear();
            contactList.addAll(temporalList);
            List<Contact> filteredList = contactList.stream()
                    .filter(c ->
                            (c.getFirstName().toLowerCase().startsWith(input.toLowerCase()))
                                    || (c.getLastName().toLowerCase().startsWith(input.toLowerCase())))
                    .collect(Collectors.toList());

            contactList.clear();
            contactList.addAll(filteredList);
            adapter.notifyDataSetChanged();
        } else {
            Toast.makeText(this, R.string.search_unavailable, Toast.LENGTH_SHORT).show();
        }
    }
}