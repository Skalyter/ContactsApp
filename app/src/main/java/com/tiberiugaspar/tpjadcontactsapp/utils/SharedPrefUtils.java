package com.tiberiugaspar.tpjadcontactsapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.tiberiugaspar.tpjadcontactsapp.R;

/**
 * An utility class to handle sharedPreferences requests
 */
public class SharedPrefUtils {

    /**
     * Used to retrieve the userId stored in the SharedPreferences
     *
     * @param applicationContext the application context needed to get the shared preferences
     * @return the userId value if exists in SharedPreferences; null otherwise.
     */
    public static String getUserId(Context applicationContext) {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(applicationContext);
        return sharedPreferences.getString(applicationContext.getString(R.string.key_user_id), null);
    }

}
