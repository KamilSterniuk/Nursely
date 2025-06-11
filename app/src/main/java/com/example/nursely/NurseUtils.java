package com.example.nursely;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class NurseUtils {

    /** Zwraca fullName pielęgniarki po loginie; fallback: login */
    public static String getFullNameByLogin(Context ctx, String login) {
        try {
            File file = new File(ctx.getFilesDir(), "users.json");
            BufferedReader br = new BufferedReader(new FileReader(file));
            String json = br.readLine();      // jedna linia – wygenerowany plik
            br.close();

            JSONArray users = new JSONArray(json);
            for (int i = 0; i < users.length(); i++) {
                JSONObject u = users.getJSONObject(i);
                if (u.getString("login").equals(login)) {
                    return u.optString("fullName", login);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return login;
    }

    /** Numer telefonu pielęgniarki lub pusty łańcuch */
    public static String getPhoneByLogin(Context ctx, String login) {
        try {
            File file = new File(ctx.getFilesDir(), "users.json");
            BufferedReader br = new BufferedReader(new FileReader(file));
            String json = br.readLine();
            br.close();

            JSONArray users = new JSONArray(json);
            for (int i = 0; i < users.length(); i++) {
                JSONObject u = users.getJSONObject(i);
                if (u.getString("login").equals(login)) {
                    return u.optString("phone", "");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
