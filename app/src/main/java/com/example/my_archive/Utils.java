package com.example.my_archive;

import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public final class Utils {

    private Utils() {
    }


    public static String sha256(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(text.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static String isoToDisplay(String iso) {
        if (iso == null) {
            return "";
        }
        try {
            SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat out = new SimpleDateFormat("dd.MM.yyyy", Locale.US);
            Date d = in.parse(iso);
            return d == null ? iso : out.format(d);
        } catch (ParseException e) {
            return iso;
        }
    }


    public static String displayToIso(String display) {
        if (display == null) {
            return "";
        }
        try {
            SimpleDateFormat in = new SimpleDateFormat("dd.MM.yyyy", Locale.US);
            SimpleDateFormat out = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date d = in.parse(display);
            return d == null ? display : out.format(d);
        } catch (ParseException e) {
            return display;
        }
    }

    public static String todayIso() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    }
}


