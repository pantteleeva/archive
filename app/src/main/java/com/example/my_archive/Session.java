package com.example.my_archive;

import android.app.Activity;
import android.content.Intent;


public final class Session {

    public static boolean loggedIn = false;
    public static String userName = "";

    private Session() {
    }

    public static boolean require(Activity activity) {
        if (loggedIn) {
            return true;
        }
        openLogin(activity);
        return false;
    }

    public static void logout(Activity activity) {
        loggedIn = false;
        userName = "";
        openLogin(activity);
    }

    private static void openLogin(Activity activity) {
        Intent i = new Intent(activity, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(i);
        activity.finish();
    }
}

