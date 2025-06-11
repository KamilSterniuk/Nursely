package com.example.nursely;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import org.json.*;
import java.io.*;
import java.util.Calendar;

public class AddEventActivity extends AppCompatActivity {

    EditText nameInput, addressInput, phoneInput, detailsInput, notesInput;
    Button dateButton, timeButton, saveButton;
    String selectedDate = "", selectedTime = "";
    String login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        login = getIntent().getStringExtra("login");

        nameInput = findViewById(R.id.nameInput);
        addressInput = findViewById(R.id.addressInput);
        phoneInput = findViewById(R.id.phoneInput);
        detailsInput = findViewById(R.id.detailsInput);
        notesInput = findViewById(R.id.notesInput);
        dateButton = findViewById(R.id.dateButton);
        timeButton = findViewById(R.id.timeButton);
        saveButton = findViewById(R.id.saveButton);

        dateButton.setOnClickListener(v -> pickDate());
        timeButton.setOnClickListener(v -> pickTime());

        saveButton.setOnClickListener(v -> saveEvent());
    }

    private void pickDate() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
            dateButton.setText("📅 " + selectedDate);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void pickTime() {
        Calendar calendar = Calendar.getInstance();
        new TimePickerDialog(this, (view, hour, minute) -> {
            selectedTime = String.format("%02d:%02d", hour, minute);
            timeButton.setText("⏰ " + selectedTime);
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
    }

    private void saveEvent() {
        String name = nameInput.getText().toString().trim();
        String address = addressInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String details = detailsInput.getText().toString().trim();
        String notes = notesInput.getText().toString().trim();

        if (name.isEmpty() || address.isEmpty() || phone.isEmpty() || details.isEmpty() || selectedDate.isEmpty() || selectedTime.isEmpty()) {
            Toast.makeText(this, "Uzupełnij wszystkie wymagane pola", Toast.LENGTH_SHORT).show();
            return;
        }

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
                    JSONArray events = user.optJSONArray("events");
                    if (events == null) {
                        events = new JSONArray();
                        user.put("events", events);
                    }

                    JSONObject newEvent = new JSONObject();
                    newEvent.put("date", selectedDate);
                    newEvent.put("time", selectedTime);
                    newEvent.put("name", name);
                    newEvent.put("address", address);
                    newEvent.put("phone", phone);
                    newEvent.put("details", details);
                    newEvent.put("notes", notes);

                    events.put(newEvent);

                    FileWriter writer = new FileWriter(file);
                    writer.write(users.toString(2));
                    writer.close();

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
}
