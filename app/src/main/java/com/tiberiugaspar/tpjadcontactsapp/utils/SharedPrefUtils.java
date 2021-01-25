package com.tiberiugaspar.tpjadcontactsapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.tiberiugaspar.tpjadcontactsapp.R;

public class SharedPrefUtils {

    public static String getUserId(Context applicationContext) {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(applicationContext);
        return sharedPreferences.getString(applicationContext.getString(R.string.key_user_id), null);
    }

}
