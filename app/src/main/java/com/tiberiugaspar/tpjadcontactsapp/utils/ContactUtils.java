package com.tiberiugaspar.tpjadcontactsapp.utils;

import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.tiberiugaspar.tpjadcontactsapp.models.Contact;

public class ContactUtils {

    private static final ColorGenerator generator = ColorGenerator.MATERIAL;

    public static String getContactInitials(Contact contact){
        String initials = contact.getFirstName().substring(0, 1);
        if (contact.getLastName() != null && contact.getLastName().length() > 1) {
            initials = String.format("%s%s", initials, contact.getLastName().substring(0, 1));
        }
        return initials.toUpperCase();
    }

    public static int getRandomColor() {
        return generator.getRandomColor();
    }

    public static String getCategoryForPosition(int position) {
        switch (position) {
            case 0:
                return "No label";
            case 1:
                return "Mobile";
            case 2:
                return "Home";
            case 3:
                return "Work";
            case 4:
                return "Main";
            case 5:
                return "Work fax";
            case 6:
                return "Home fax";
            default:
                return "Others";
        }
    }
}
