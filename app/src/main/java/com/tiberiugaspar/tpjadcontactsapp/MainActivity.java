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
import com.tiberiugaspar.tpjadcontactsapp.utils.EncryptionUtils;
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
        //set the layout from xml file
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.contacts);
        setSupportActionBar(toolbar);

        //instantiate the FirebaseFirestore object
        db = FirebaseFirestore.getInstance();

        //initialize the views
        initializeViews();
    }

    private void initializeViews() {

        //setting the floating action button for adding a new contact
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //starting AddEditContactActivity for result
                Intent intent = new Intent(MainActivity.this, AddEditContactActivity.class);
                startActivityForResult(intent, REQ_CODE_ADD_CONTACT);
            }
        });

        RecyclerView recyclerView = findViewById(R.id.recycler_contacts);

        //initializing the adapter
        adapter = new ContactAdapter(MainActivity.this, contactList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(
                new LinearLayoutManager(MainActivity.this,
                        RecyclerView.VERTICAL,
                        false));

        //get userId from sharedPreferences
        userId = SharedPrefUtils.getUserId(getApplicationContext());

        //get contactList from database for the given userId
        getContactsFromDb();

        //create a swipeController object to handle swipe gestures over the recyclerView items
        SwipeController swipeController = new SwipeController(this, new SwipeControllerActions() {

            //implement onLeftClicked and onRightClicked methods from abstract class SwipeControllerActions
            @Override
            public void onLeftClicked(int position) {

                //onLeftClicked we start the Dial activity, sending the user to the Phone app
                // with the auto-completed phone number to facilitate the call action
                String phoneNumber = contactList.get(position)
                        .getPhoneNumberList()
                        .get(0).getPhoneNumber();

                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse(String.format("tel:%s", phoneNumber)));
                startActivity(callIntent);
            }

            @Override
            public void onRightClicked(int position) {

                //onRightClicked we start the SMS conversation activity, sending the user to the
                // default SMS app, starting automatically a new conversation (if no existing one is
                // available) with the corresponding phone number
                String phoneNumber = contactList.get(position)
                        .getPhoneNumberList()
                        .get(0).getPhoneNumber();

                Intent smsIntent = new Intent(Intent.ACTION_VIEW);

                smsIntent.setData(Uri.parse(String.format("sms:%s", phoneNumber)));
                startActivity(smsIntent);
            }
        });

        //create a new ItemTouchHelper object which requires a callback (our swipeController)
        ItemTouchHelper touchHelper = new ItemTouchHelper(swipeController);

        //attach touchHelper to our recyclerView
        touchHelper.attachToRecyclerView(recyclerView);

        //addItemDecoration over our recyclerView to draw the buttons when swipe gestures are captured
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, @NonNull RecyclerView.State state) {
                swipeController.onDraw(c);
            }
        });
    }

    /**
     * Auto-generated method for Search bar - no-op
     *
     * @param query user's input in the search bar
     * @return false - as no-op is done here
     */
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    /**
     * This method is called for every text update in the search bar
     *
     * @param newText the new text from user input, updated once for every character added/removed
     *                from the search bar
     * @return true - as we update the list of contacts accordingly
     */
    @Override
    public boolean onQueryTextChange(String newText) {
        if (newText == null || newText.length() < 1) {
            //if the user input is null or its length is lower than 1
            // it means that the user has introduced nothing
            // and we have to show him all the contacts
            contactList.clear();

            //we add all the contacts stored temporally in the temporalList
            // to avoid querying the results from FirebaseFirestore for each search
            contactList.addAll(temporalList);

            //ultimately we notify the adapter that the dataset has changed
            adapter.notifyDataSetChanged();

        } else {
            //if the user input is not null and its length is bigger than 1
            // we call the getContactsForInput method to display only the matching items
            getContactsForInput(newText);
        }

        //returning true as the method served its purpose
        return true;
    }

    /**
     * @param menu the menu object that holds the items
     * @return true - because the creation of optionMenu was successful
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //inflate the menu from the xml resource
        getMenuInflater().inflate(R.menu.main_menu, menu);

        //find the searchItem inside the menu
        MenuItem searchItem = menu.findItem(R.id.search_bar);

        //initialize the searchView
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getString(R.string.search_contacts));
        searchView.setOnQueryTextListener(this);
        searchView.setIconified(true);

        //return true as the method served its purpose
        return true;
    }

    /**
     * @param item the item that was selected from the menu item list
     * @return true if the item was the logout_button; its super() method otherwise.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.button_logout) {

            //if the user selects the logout button we get its instance and we sign him/her out
            FirebaseAuth auth = FirebaseAuth.getInstance();
            auth.signOut();

            //we start a new LoginActivity, allowing the user to switch its account or to log in again
            startActivity(new Intent(MainActivity.this, LoginActivity.class));

            //we finish the current activity
            finish();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        //for any of the 2 request codes, if the result code is RESULT_OK, we update our contact list
        // with a new select query to our database
        if (requestCode == REQ_CODE_ADD_CONTACT && resultCode == RESULT_OK) {

            getContactsFromDb();
        }
        if (requestCode == REQ_CODE_EDIT_CONTACT && resultCode == RESULT_OK) {

            getContactsFromDb();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void getContactsFromDb() {
        //select query to get contacts from db by the user Id and order them by the first name
        db.collection("contacts")
                .whereEqualTo("userId", userId)
                .orderBy("firstName", Query.Direction.ASCENDING)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                //add OnSuccessListener

                //clear the initial contactList
                contactList.clear();
                for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                    Contact contact = snapshot.toObject(Contact.class);
                    //decrypt each individual contact and
                    //add them in the contactList
                    contactList.add(EncryptionUtils.decryptContact(contact));
                }
                //notify the adapter that the dataset has changed
                adapter.notifyDataSetChanged();
                //propagate the changes to the temporalList, preparing it for further search operations
                temporalList.clear();
                temporalList.addAll(contactList);
            }
        });
    }

    /**
     * Update the list, showing only items that matches the search string
     *
     * @param input the input of the user in the SearchBar
     */
    private void getContactsForInput(String input) {

        //first we perform a quick SDK version check
        // because we are going to use Java Streams (which are available in Android only
        // from Android 7 (with its Nougat code /VERSION_CODES.N)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {

            //we clear the contact list and add all items from temporal list
            // to ensure that for each added/removed character we perform the search over the entire
            // list of contacts
            contactList.clear();
            contactList.addAll(temporalList);

            //we perform the stream, where we filter the results by the contact's first name or last name
            // for which we check whether they match or not
            // ultimately we collect the resulted stream, assigning it to the filteredList
            List<Contact> filteredList = contactList.stream()
                    .filter(c ->
                            (c.getFirstName().toLowerCase().startsWith(input.toLowerCase()))
                                    || (c.getLastName().toLowerCase().startsWith(input.toLowerCase())))
                    .collect(Collectors.toList());

            //we clear once more the contact list
            contactList.clear();
            //we add only the items that matches the search input
            contactList.addAll(filteredList);
            //and we notify adapter that the dataset has changed
            adapter.notifyDataSetChanged();
        } else {

            //if the Android Version it older than Android 7, we show a toast, notifying the user
            // that this functionality is not available on his/her device.
            Toast.makeText(this, R.string.search_unavailable, Toast.LENGTH_SHORT).show();
        }
    }
}