package com.tiberiugaspar.tpjadcontactsapp.models;

import java.util.ArrayList;
import java.util.List;

/**
 * The model class for any Contact instance
 *
 * <p>A contact must have a userId, a contactId, firstName and at least one phone number to be stored
 * in the database.</p>
 * <p>All the other fields are optional, therefore are treated accordingly.</p>
 */
public class Contact {

    private String userId;
    private String contactId;
    private String firstName;
    private String lastName;
    private String email;
    private List<PhoneNumber> phoneNumberList = new ArrayList<>();
    private String uriToImage;

    public Contact() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * To avoid a {@link NullPointerException}, if the lastName is null, we set it to the empty string
     * value, for display purposes.
     *
     * @return the lastName if it is not null; an empty string otherwise.
     */
    public String getLastName() {
        if (lastName == null) lastName = "";
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<PhoneNumber> getPhoneNumberList() {
        return phoneNumberList;
    }

    public void setPhoneNumberList(List<PhoneNumber> phoneNumberList) {
        this.phoneNumberList = phoneNumberList;
    }

    public String getUriToImage() {
        return uriToImage;
    }

    public void setUriToImage(String uriToImage) {
        this.uriToImage = uriToImage;
    }
}
