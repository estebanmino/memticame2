package com.memeticame.memeticame.models;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by ESTEBANFML on 01-10-2017.
 */

public class SharedPreferencesClass {

    private SharedPreferences sharedUserPrefs;
    private SharedPreferences.Editor sharedEditorPrefs;

    public void setUsersPreferences(SharedPreferences sharedPreferences, String email, String phone) {
        sharedUserPrefs = sharedPreferences;
        sharedEditorPrefs = sharedPreferences.edit();
        sharedEditorPrefs.putString("phone",phone);
        sharedEditorPrefs.putString("email",email);

    }

}
