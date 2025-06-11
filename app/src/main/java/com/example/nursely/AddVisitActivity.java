package com.example.nursely;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import org.json.*;
import java.io.*;
import java.util.*;

public class AddVisitActivity extends AppCompatActivity {

    EditText nameInput, addressInput, phoneInput, detailsInput, notesInput;
    Button dateBtn, timeBtn, saveBtn;
    Spinner nurseSpinner;
    String selectedDate = "", selectedTime = "";
    String fixedLogin = null;   // jeśli przychodzimy z plusa grupy

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_visit);  // patrz następny punkt

        // pola
        nurseSpinner = findViewById(R.id.nurseSpinner);
        nameInput    = findViewById(R.id.nameInput);
        addressInput = findViewById(R.id.addressInput);
        phoneInput   = findViewById(R.id.phoneInput);
        detailsInput = findViewById(R.id.detailsInput);
        notesInput   = findViewById(R.id.notesInput);
        dateBtn      = findViewById(R.id.dateButton);
        timeBtn      = findViewById(R.id.timeButton);
        saveBtn      = findViewById(R.id.saveButton);

        fixedLogin = getIntent().getStringExtra("login"); // może być null

        setupNurseSpinner();

        dateBtn.setOnClickListener(v -> pickDate());
        timeBtn.setOnClickListener(v -> pickTime());
        saveBtn.setOnClickListener(v -> saveVisit());

        Button addNurseButton = findViewById(R.id.addNurseButton);
        addNurseButton.setOnClickListener(v -> showAddNurseDialog());


    }

    /* wypełnij spinner listą pielęgniarek */
    private void setupNurseSpinner() {
        List<String> logins = new ArrayList<>();
        try {
            File file = new File(getFilesDir(), "users.json");
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();
            JSONArray users = new JSONArray(sb.toString());

            for (int i = 0; i < users.length(); i++) {
                JSONObject u = users.getJSONObject(i);
                if (u.getString("userType").equals("nurse"))
                    logins.add(u.getString("login"));
            }
        } catch (Exception e) { e.printStackTrace(); }

        ArrayAdapter<String> aa = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, logins);
        nurseSpinner.setAdapter(aa);

        if (fixedLogin != null) {  // ukryj spinner
            nurseSpinner.setSelection(logins.indexOf(fixedLogin));
            nurseSpinner.setEnabled(false);
        }
    }

    private void pickDate() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (v, y, m, d) -> {
            selectedDate = String.format("%04d-%02d-%02d", y, m + 1, d);
            dateBtn.setText("📅 " + selectedDate);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void pickTime() {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(this, (v, h, m) -> {
            selectedTime = String.format("%02d:%02d", h, m);
            timeBtn.setText("⏰ " + selectedTime);
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
    }

    private void saveVisit() {
        String nurseLogin = nurseSpinner.getSelectedItem().toString();
        String name = nameInput.getText().toString().trim();
        String address = addressInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String details = detailsInput.getText().toString().trim();
        String notes = notesInput.getText().toString().trim();

        if (name.isEmpty() || address.isEmpty() || phone.isEmpty()
                || details.isEmpty() || selectedDate.isEmpty() || selectedTime.isEmpty()) {
            Toast.makeText(this, "Uzupełnij wszystkie pola!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File file = new File(getFilesDir(), "users.json");
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder sb = new StringBuilder();
            String line; while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();
            JSONArray users = new JSONArray(sb.toString());

            for (int i = 0; i < users.length(); i++) {
                JSONObject u = users.getJSONObject(i);
                if (u.getString("login").equals(nurseLogin)) {
                    JSONArray events = u.optJSONArray("events");
                    if (events == null) { events = new JSONArray(); u.put("events", events); }

                    JSONObject newE = new JSONObject();
                    newE.put("date", selectedDate);
                    newE.put("time", selectedTime);
                    newE.put("name", name);
                    newE.put("address", address);
                    newE.put("phone", phone);
                    newE.put("details", details);
                    newE.put("notes", notes);
                    events.put(newE);

                    // posortuj po dacie
                    List<JSONObject> list = new ArrayList<>();
                    for (int j = 0; j < events.length(); j++) list.add(events.getJSONObject(j));
                    list.sort(Comparator.comparing(o -> o.optString("date") + " " + o.optString("time")));
                    JSONArray sorted = new JSONArray();
                    for (JSONObject obj : list) sorted.put(obj);
                    u.put("events", sorted);

                    FileWriter w = new FileWriter(file);
                    w.write(users.toString(2));
                    w.close();
                    Toast.makeText(this, "Wizyta dodana", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Błąd zapisu wizyty", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAddNurseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Dodaj pielęgniarkę");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_nurse, null);
        builder.setView(dialogView);

        EditText loginInput = dialogView.findViewById(R.id.loginInput);
        EditText passwordInput = dialogView.findViewById(R.id.passwordInput);
        EditText phoneInput = dialogView.findViewById(R.id.phoneInput);
        EditText fullNameInput = dialogView.findViewById(R.id.fullNameInput);

        builder.setPositiveButton("Zapisz", (dialog, which) -> {
            String login = loginInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String phone = phoneInput.getText().toString().trim();
            String fullName = fullNameInput.getText().toString().trim();

            if (login.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Uzupełnij login i hasło", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                File file = new File(getFilesDir(), "users.json");
                if (!file.exists()) {
                    JSONArray initial = new JSONArray();
                    FileWriter writer = new FileWriter(file);
                    writer.write(initial.toString());
                    writer.close();
                }

                BufferedReader reader = new BufferedReader(new FileReader(file));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();

                JSONArray users = new JSONArray(sb.toString());

                for (int i = 0; i < users.length(); i++) {
                    if (users.getJSONObject(i).getString("login").equals(login)) {
                        Toast.makeText(this, "Login już istnieje", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                JSONObject newNurse = new JSONObject();
                newNurse.put("login", login);
                newNurse.put("password", password);
                newNurse.put("userType", "nurse");
                newNurse.put("phone", phone);
                newNurse.put("fullName", fullName);
                newNurse.put("events", new JSONArray());

                users.put(newNurse);

                FileWriter writer = new FileWriter(file);
                writer.write(users.toString(2));
                writer.close();

                Toast.makeText(this, "Pielęgniarka dodana", Toast.LENGTH_SHORT).show();
                recreate();

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Błąd zapisu: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        builder.setNegativeButton("Anuluj", null);
        builder.show();
    }



}
