// === EventDetailActivity.java z opcją dzwonienia ===
package com.example.nursely;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.content.pm.PackageManager;
import android.Manifest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class EventDetailActivity extends AppCompatActivity {

    TextView nameText, phoneText, addressText, detailsText;
    EditText notesEditText;
    Button openMapBtn, editBtn, deleteBtn, saveNoteBtn, callBtn;
    String login, date, time;
    EventItem currentEvent;
    private static final int REQUEST_CALL_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        nameText = findViewById(R.id.nameText);
        phoneText = findViewById(R.id.phoneText);
        addressText = findViewById(R.id.addressText);
        detailsText = findViewById(R.id.detailsText);
        notesEditText = findViewById(R.id.notesEditText);

        openMapBtn = findViewById(R.id.openMapButton);
        editBtn = findViewById(R.id.editButton);
        deleteBtn = findViewById(R.id.deleteButton);
        saveNoteBtn = findViewById(R.id.saveNoteButton);

        login = getIntent().getStringExtra("login");
        date = getIntent().getStringExtra("date");
        time = getIntent().getStringExtra("time");

        Button callButton = findViewById(R.id.callButton);
        callButton.setOnClickListener(v -> {
            String phone = currentEvent.getPhone();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 1);
            } else {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + phone));
                startActivity(callIntent);
            }
        });


        loadEvent();

        openMapBtn.setOnClickListener(v -> {
            Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(currentEvent.getAddress()));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        });

        editBtn.setOnClickListener(v -> {
            Toast.makeText(this, "Tryb edycji (do zaimplementowania)", Toast.LENGTH_SHORT).show();
        });

        deleteBtn.setOnClickListener(v -> {
            Toast.makeText(this, "Potwierdzenie usunięcia (do zaimplementowania)", Toast.LENGTH_SHORT).show();
        });

        saveNoteBtn.setOnClickListener(v -> saveNoteToJson());

    }

    private void makePhoneCall() {
        String phoneNumber = currentEvent.getPhone();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PERMISSION);
            return;
        }
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(callIntent);
    }

    private void loadEvent() {
        try {
            File file = new File(getFilesDir(), "users.json");
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();

            JSONArray users = new JSONArray(sb.toString());
            for (int i = 0; i < users.length(); i++) {
                JSONObject user = users.getJSONObject(i);
                if (user.getString("login").equals(login)) {
                    JSONArray events = user.getJSONArray("events");
                    for (int j = 0; j < events.length(); j++) {
                        JSONObject e = events.getJSONObject(j);
                        if (e.getString("date").equals(date) && e.getString("time").equals(time)) {
                            currentEvent = new EventItem(
                                    e.getString("date"),
                                    e.getString("time"),
                                    e.getString("name"),
                                    e.getString("address"),
                                    e.optString("phone", ""),
                                    e.optString("details", ""),
                                    e.optString("notes", "")
                            );
                            nameText.setText(currentEvent.getPatientName());
                            phoneText.setText(currentEvent.getPhone());
                            addressText.setText(currentEvent.getAddress());
                            detailsText.setText(currentEvent.getDetails());
                            notesEditText.setText(currentEvent.getNotes());
                            return;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Błąd ładowania wydarzenia", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveNoteToJson() {
        try {
            File file = new File(getFilesDir(), "users.json");
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();

            JSONArray users = new JSONArray(sb.toString());
            for (int i = 0; i < users.length(); i++) {
                JSONObject user = users.getJSONObject(i);
                if (user.getString("login").equals(login)) {
                    JSONArray events = user.getJSONArray("events");
                    for (int j = 0; j < events.length(); j++) {
                        JSONObject e = events.getJSONObject(j);
                        if (e.getString("date").equals(date) && e.getString("time").equals(time)) {
                            String newNote = notesEditText.getText().toString();
                            e.put("notes", newNote);
                            FileWriter writer = new FileWriter(file);
                            writer.write(users.toString(2));
                            writer.close();
                            Toast.makeText(this, "Notatka zapisana", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                }
            }

            Toast.makeText(this, "Nie znaleziono wydarzenia do zapisu", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Błąd zapisu notatki", Toast.LENGTH_SHORT).show();
        }
    }
}