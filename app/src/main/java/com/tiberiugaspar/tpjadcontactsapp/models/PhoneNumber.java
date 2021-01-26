package com.tiberiugaspar.tpjadcontactsapp.models;

/**
 * The model class for any PhoneNumber instance.
 * <p>It is subordinated to the {@link Contact} class, as a phoneNumber must be assigned to a Contact
 * object, and a contact object might have one or many phoneNumbers.</p>
 * <p>It contains a string holding the actual phoneNumber and an integer value to store the category,
 * as follows: case 0: "No label";
 * case 1: "Mobile";
 * case 2: "Home";
 * case 3: "Work";
 * case 4: "Main";
 * case 5: "Work fax";
 * case 6: "Home fax";
 * default: "Others";
 * </p>
 */
public class PhoneNumber {
    private String phoneNumber;
    private int category;

    public PhoneNumber() {
    }

    public PhoneNumber(String phoneNumber, int category) {
        this.phoneNumber = phoneNumber;
        this.category = category;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }
}
