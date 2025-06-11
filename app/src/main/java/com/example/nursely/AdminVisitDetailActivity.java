package com.example.nursely;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import org.json.*;
import java.io.*;
import java.util.*;

public class AdminVisitDetailActivity extends AppCompatActivity {

    TextView nameText, addressText, phoneText, detailsText;
    TextView visitDateText, visitTimeText, nurseLoginText;
    Button editBtn, deleteBtn, transferBtn;
    String login, date, time;
    JSONObject currentEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_visit_detail);

        nameText = findViewById(R.id.nameText);
        addressText = findViewById(R.id.addressText);
        phoneText = findViewById(R.id.phoneText);
        detailsText = findViewById(R.id.detailsText);
        editBtn = findViewById(R.id.editButton);
        deleteBtn = findViewById(R.id.deleteButton);
        transferBtn = findViewById(R.id.transferButton);
        visitDateText = findViewById(R.id.visitDateText);
        visitTimeText = findViewById(R.id.visitTimeText);
        nurseLoginText = findViewById(R.id.nurseLoginText);


        login = getIntent().getStringExtra("login");
        date = getIntent().getStringExtra("event_date");
        time = getIntent().getStringExtra("event_time");

        visitDateText.setText("📅 Data: " + date);
        visitTimeText.setText("🕒 Godzina: " + time);
        nurseLoginText.setText("👩‍⚕️ Pielęgniarka: " + login);


        try {
            currentEvent = new JSONObject();
            currentEvent.put("date", date);
            currentEvent.put("time", time);
            currentEvent.put("name", getIntent().getStringExtra("event_name"));
            currentEvent.put("address", getIntent().getStringExtra("event_address"));
            currentEvent.put("phone", getIntent().getStringExtra("event_phone"));
            currentEvent.put("details", getIntent().getStringExtra("event_details"));
            currentEvent.put("notes", "");

            nameText.setText(currentEvent.getString("name"));
            addressText.setText(currentEvent.getString("address"));
            phoneText.setText(currentEvent.getString("phone"));
            detailsText.setText(currentEvent.getString("details"));

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Błąd ładowania", Toast.LENGTH_SHORT).show();
        }

        editBtn.setOnClickListener(v -> showEditDialog());
        deleteBtn.setOnClickListener(v -> confirmDelete());
        transferBtn.setOnClickListener(v -> showTransferDialog());
    }

    private void showEditDialog() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            new TimePickerDialog(this, (view1, hour, minute) -> {
                String newDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                String newTime = String.format("%02d:%02d", hour, minute);
                updateDateTime(newDate, newTime);
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDateTime(String newDate, String newTime) {
        try {
            File file = new File(getFilesDir(), "users.json");
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();
            JSONArray users = new JSONArray(sb.toString());

            for (int i = 0; i < users.length(); i++) {
                JSONObject user = users.getJSONObject(i);
                if (user.getString("login").equals(login)) {
                    JSONArray events = user.getJSONArray("events");
                    for (int j = 0; j < events.length(); j++) {
                        JSONObject e = events.getJSONObject(j);
                        if (e.getString("date").equals(date) && e.getString("time").equals(time)) {
                            e.put("date", newDate);
                            e.put("time", newTime);
                            FileWriter writer = new FileWriter(file);
                            writer.write(users.toString(2));
                            writer.close();
                            Toast.makeText(this, "Zmieniono termin wizyty", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Błąd edycji", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Usunąć wizytę?")
                .setMessage("Tej operacji nie można cofnąć.")
                .setPositiveButton("Tak", (dialog, which) -> deleteVisit())
                .setNegativeButton("Nie", null)
                .show();
    }

    private void deleteVisit() {
        try {
            File file = new File(getFilesDir(), "users.json");
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();
            JSONArray users = new JSONArray(sb.toString());

            for (int i = 0; i < users.length(); i++) {
                JSONObject user = users.getJSONObject(i);
                if (user.getString("login").equals(login)) {
                    JSONArray events = user.getJSONArray("events");
                    for (int j = 0; j < events.length(); j++) {
                        JSONObject e = events.getJSONObject(j);
                        if (e.getString("date").equals(date) && e.getString("time").equals(time)) {
                            events.remove(j);
                            FileWriter writer = new FileWriter(file);
                            writer.write(users.toString(2));
                            writer.close();
                            Toast.makeText(this, "Wizyta usunięta", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Błąd usuwania", Toast.LENGTH_SHORT).show();
        }
    }

    private void showTransferDialog() {
        try {
            File file = new File(getFilesDir(), "users.json");
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();
            JSONArray users = new JSONArray(sb.toString());

            List<String> nurseLogins = new ArrayList<>();
            for (int i = 0; i < users.length(); i++) {
                JSONObject user = users.getJSONObject(i);
                if (user.getString("userType").equals("nurse") && !user.getString("login").equals(login)) {
                    nurseLogins.add(user.getString("login"));
                }
            }

            String[] options = nurseLogins.toArray(new String[0]);

            new AlertDialog.Builder(this)
                    .setTitle("Wybierz pielęgniarkę docelową")
                    .setItems(options, (dialog, which) -> transferVisitTo(users, options[which]))
                    .show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Błąd pobierania pielęgniarek", Toast.LENGTH_SHORT).show();
        }
    }

    private void transferVisitTo(JSONArray users, String targetLogin) {
        try {
            JSONObject visitCopy = new JSONObject(currentEvent.toString());

            for (int i = 0; i < users.length(); i++) {
                JSONObject user = users.getJSONObject(i);
                if (user.getString("login").equals(login)) {
                    JSONArray events = user.getJSONArray("events");
                    for (int j = 0; j < events.length(); j++) {
                        JSONObject e = events.getJSONObject(j);
                        if (e.getString("date").equals(date) && e.getString("time").equals(time)) {
                            events.remove(j);
                            break;
                        }
                    }
                }
                if (user.getString("login").equals(targetLogin)) {
                    JSONArray events = user.optJSONArray("events");
                    if (events == null) {
                        events = new JSONArray();
                        user.put("events", events);
                    }
                    events.put(visitCopy);
                }
            }

            File file = new File(getFilesDir(), "users.json");
            FileWriter writer = new FileWriter(file);
            writer.write(users.toString(2));
            writer.close();

            Toast.makeText(this, "Przeniesiono wizytę", Toast.LENGTH_SHORT).show();
            finish();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Błąd przenoszenia", Toast.LENGTH_SHORT).show();
        }
    }
}
