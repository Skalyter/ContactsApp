package com.tiberiugaspar.tpjadcontactsapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.tiberiugaspar.tpjadcontactsapp.adapters.ContactAdapter;
import com.tiberiugaspar.tpjadcontactsapp.models.Contact;
import com.tiberiugaspar.tpjadcontactsapp.models.PhoneNumber;
import com.tiberiugaspar.tpjadcontactsapp.utils.SwipeController;
import com.tiberiugaspar.tpjadcontactsapp.utils.SwipeControllerActions;
import com.tiberiugaspar.tpjadcontactsapp.utils.TAGS;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.ItemTouchHelper.Callback;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static com.tiberiugaspar.tpjadcontactsapp.utils.TAGS.REQ_CODE_ADD_CONTACT;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private List<Contact> contactList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ContactAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.contacts);
        setSupportActionBar(toolbar);

        findViewsByIds();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddEditContactActivity.class);
                startActivityForResult(intent, REQ_CODE_ADD_CONTACT);
            }
        });
    }

    private void findViewsByIds(){
        recyclerView = findViewById(R.id.recycler_contacts);

        //TODO: delete/comment these lines after debugging
        Contact contactDummy = new Contact();
        contactDummy.setFirstName("Ion");
        contactDummy.setLastName("Ionescu");
        PhoneNumber phoneNumberDummy = new PhoneNumber("07555484333", 0);
        contactDummy.getPhoneNumberList().add(phoneNumberDummy);
        contactList.add(contactDummy);
        contactList.add(contactDummy);

        adapter = new ContactAdapter(this, contactList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(
                new LinearLayoutManager(this,
                        RecyclerView.VERTICAL,
                        false));
        SwipeController swipeController = new SwipeController(this, new SwipeControllerActions() {
            @Override
            public void onLeftClicked(int position) {

                String phoneNumber = contactList.get(position)
                        .getPhoneNumberList()
                        .get(0).getPhoneNumber();

                Intent callIntent = new Intent(Intent.ACTION_DIAL);

                callIntent.setData(Uri.parse("tel:"+phoneNumber));
                startActivity(callIntent);
            }

            @Override
            public void onRightClicked(int position) {

                String phoneNumber = contactList.get(position)
                        .getPhoneNumberList()
                        .get(0).getPhoneNumber();

                Intent smsIntent = new Intent(Intent.ACTION_VIEW);

                smsIntent.setData(Uri.parse("sms:"+phoneNumber));
                startActivity(smsIntent);
            }
        });
        ItemTouchHelper touchHelper = new ItemTouchHelper(swipeController);
        touchHelper.attachToRecyclerView(recyclerView);
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                swipeController.onDraw(c);
            }
        });
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Toast.makeText(MainActivity.this, query, Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
//        Toast.makeText(MainActivity.this, newText, Toast.LENGTH_SHORT).show();
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQ_CODE_ADD_CONTACT && resultCode == RESULT_OK){
            //TODO: actualizam lista
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}