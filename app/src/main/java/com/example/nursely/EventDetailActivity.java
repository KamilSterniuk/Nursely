package com.example.nursely;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Iterator;

public class EventDetailActivity extends AppCompatActivity {

    TextView nameText, phoneText, addressText, detailsText;
    EditText dateEdit, timeEdit, notesEdit;
    Button openMapBtn, saveBtn, deleteBtn;
    String login, date, time;
    EventItem currentEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        nameText = findViewById(R.id.patientName);
        phoneText = findViewById(R.id.patientPhone);
        addressText = findViewById(R.id.patientAddress);
        detailsText = findViewById(R.id.patientDetails);
        dateEdit = findViewById(R.id.dateEdit);
        timeEdit = findViewById(R.id.timeEdit);
        notesEdit = findViewById(R.id.notesEdit);
        openMapBtn = findViewById(R.id.openMapButton);
        saveBtn = findViewById(R.id.saveButton);
        deleteBtn = findViewById(R.id.deleteButton);

        login = getIntent().getStringExtra("login");
        date = getIntent().getStringExtra("date");
        time = getIntent().getStringExtra("time");

        loadEvent();

        openMapBtn.setOnClickListener(v -> {
            Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(currentEvent.getAddress()));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        });

        saveBtn.setOnClickListener(v -> {
            updateEvent();
            Toast.makeText(this, "Zapisano zmiany", Toast.LENGTH_SHORT).show();
            finish();
        });

        deleteBtn.setOnClickListener(v -> {
            deleteEvent();
            Toast.makeText(this, "Usunięto wizytę", Toast.LENGTH_SHORT).show();
            finish();
        });
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
                            dateEdit.setText(currentEvent.getDate());
                            timeEdit.setText(currentEvent.getTime());
                            notesEdit.setText(currentEvent.getNotes());
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

    private void updateEvent() {
        try {
            File file = new File(getFilesDir(), "users.json");
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            JSONArray users = new JSONArray(sb.toString());

            for (int i = 0; i < users.length(); i++) {
                JSONObject user = users.getJSONObject(i);
                if (user.getString("login").equals(login)) {
                    JSONArray events = user.getJSONArray("events");
                    for (int j = 0; j < events.length(); j++) {
                        JSONObject e = events.getJSONObject(j);
                        if (e.getString("date").equals(date) && e.getString("time").equals(time)) {
                            e.put("date", dateEdit.getText().toString());
                            e.put("time", timeEdit.getText().toString());
                            e.put("notes", notesEdit.getText().toString());
                            break;
                        }
                    }
                }
            }

            FileWriter writer = new FileWriter(file);
            writer.write(users.toString());
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteEvent() {
        try {
            File file = new File(getFilesDir(), "users.json");
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            JSONArray users = new JSONArray(sb.toString());

            for (int i = 0; i < users.length(); i++) {
                JSONObject user = users.getJSONObject(i);
                if (user.getString("login").equals(login)) {
                    JSONArray events = user.getJSONArray("events");
                    for (int j = events.length() - 1; j >= 0; j--) {
                        JSONObject e = events.getJSONObject(j);
                        if (e.getString("date").equals(date) && e.getString("time").equals(time)) {
                            events.remove(j);  // <-- usuwa obiekt z JSONArray
                            break;
                        }
                    }
                }
            }

            FileWriter writer = new FileWriter(file);
            writer.write(users.toString());
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
