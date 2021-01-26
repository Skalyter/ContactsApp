package com.tiberiugaspar.tpjadcontactsapp.utils;

import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.tiberiugaspar.tpjadcontactsapp.models.Contact;

/**
 * An utility class to handle basic operations with {@link Contact} objects
 */
public class ContactUtils {

    private static final ColorGenerator generator = ColorGenerator.MATERIAL;

    /**
     * @param contact the contact for which it is needed to retrieve the initials
     * @return first letter from contact's firstName and lastName (if it is not null
     * and its length is > 1)
     */
    public static String getContactInitials(Contact contact) {
        String initials = contact.getFirstName().substring(0, 1);
        if (contact.getLastName() != null && contact.getLastName().length() > 1) {
            initials = String.format("%s%s", initials, contact.getLastName().substring(0, 1));
        }
        return initials.toUpperCase();
    }

    /**
     * @return an integer value which is the equivalent of a random MaterialColor, generated
     * using the {@link ColorGenerator} object.
     */
    public static int getRandomColor() {
        return generator.getRandomColor();
    }

    /**
     * @param position the position of the category in the Spinner list
     * @return the equivalent String value for given position
     */
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
