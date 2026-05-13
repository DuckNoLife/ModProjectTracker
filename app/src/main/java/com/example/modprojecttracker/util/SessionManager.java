package com.example.modprojecttracker.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.modprojecttracker.model.User;

public class SessionManager {

    private static final String PREF_NAME = "mod_tracker_session";

    private final SharedPreferences preferences;

    public SessionManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveLogin(User user) {
        preferences.edit()
                .putBoolean(Constants.PREF_IS_LOGGED_IN, true)
                .putString(Constants.PREF_USER_ID, user.getUid())
                .putString(Constants.PREF_USER_USERNAME, user.getUsername())
                .putString(Constants.PREF_USER_NAME, user.getName())
                .putString(Constants.PREF_USER_ROLE, user.getRole())
                .apply();
    }

    public void clearSession() {
        preferences.edit().clear().apply();
    }

    public boolean isLoggedIn() {
        return preferences.getBoolean(Constants.PREF_IS_LOGGED_IN, false);
    }

    public String getUserId() {
        return preferences.getString(Constants.PREF_USER_ID, null);
    }

    public String getUsername() {
        return preferences.getString(Constants.PREF_USER_USERNAME, null);
    }

    public String getRole() {
        return preferences.getString(Constants.PREF_USER_ROLE, null);
    }

    public boolean isAdminLoggedIn() {
        return isLoggedIn() && Constants.ROLE_ADMIN.equals(getRole());
    }
}

