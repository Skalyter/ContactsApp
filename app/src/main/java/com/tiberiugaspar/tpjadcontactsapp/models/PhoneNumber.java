package com.tiberiugaspar.tpjadcontactsapp.models;

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
