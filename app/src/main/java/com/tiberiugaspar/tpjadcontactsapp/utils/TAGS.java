package com.tiberiugaspar.tpjadcontactsapp.utils;

/**
 * Class used to store int and String constants used app-wide.
 */
public class TAGS {

    /**
     * The request code for the intents used to add a new contact
     */
    public static final int REQ_CODE_ADD_CONTACT = 100;

    /**
     * The request code for the intents used to edit an existing contact
     */
    public static final int REQ_CODE_EDIT_CONTACT = 101;

    /**
     * The request code for the intents used to get photos from storage
     */
    public static final int REQ_CODE_PHOTO_PICKER = 102;

    /**
     * Extra String key used to store the value of a contact's id between the activities
     */
    public static final String EXTRA_CONTACT_ID = "extra_contact_id";

    /**
     * Regular expression String containing the pattern of a valid email address
     */
    public static final String VALID_EMAIL_ADDRESS_REGEX
            = "^[A-z0-9._%+-]+@[A-z0-9.-]+\\.[A-z]{2,6}$";


    /**
     * Interval in milliseconds in which the user might press the back button twice
     * in order to exit the application
     */
    public static final int TIME_INTERVAL = 2000;
}
