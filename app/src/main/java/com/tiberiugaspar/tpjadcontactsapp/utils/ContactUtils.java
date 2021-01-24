package com.tiberiugaspar.tpjadcontactsapp.utils;

import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.tiberiugaspar.tpjadcontactsapp.models.Contact;

public class ContactUtils {

    private static final ColorGenerator generator = ColorGenerator.MATERIAL;

    public static String getContactInitials(Contact contact){
        String initials  = contact.getFirstName().substring(0,1);
        if (contact.getLastName()!= null && contact.getLastName().length() > 1){
            initials  = String.format("%s%s", initials, contact.getLastName().substring(0, 1));
        }
        return initials;
    }

    public static int getRandomColor(){
        return generator.getRandomColor();
    }
}
