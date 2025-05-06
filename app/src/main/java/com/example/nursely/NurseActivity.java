// === NurseActivity.java (poprawiona konstrukcja EventItem) ===
package com.example.nursely;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class NurseActivity extends AppCompatActivity {

    MaterialCalendarView calendarView;
    RecyclerView recyclerView;
    EventAdapter adapter;
    List<EventItem> fullEventList = new ArrayList<>();
    String currentLogin;
    LocalDate selectedDate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nurse);

        calendarView = findViewById(R.id.calendarView);
        recyclerView = findViewById(R.id.eventRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        currentLogin = getIntent().getStringExtra("login");

        loadEventsForNurse();

        calendarView.setOnDateChangedListener((widget, calendarDay, selected) -> {
            LocalDate clickedDate = convertToLocalDate(calendarDay.getDate());

            if (selectedDate != null && selectedDate.equals(clickedDate)) {
                calendarView.clearSelection();
                selectedDate = null;
                showAllEventsGrouped();
            } else {
                selectedDate = clickedDate;
                filterEventsByDate(clickedDate);
            }
        });

        showAllEventsGrouped();
    }

    private void loadEventsForNurse() {
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
                if (user.getString("login").equals(currentLogin)) {
                    fullEventList.clear();
                    if (user.has("events")) {
                        JSONArray events = user.getJSONArray("events");
                        for (int j = 0; j < events.length(); j++) {
                            JSONObject e = events.getJSONObject(j);
                            fullEventList.add(new EventItem(
                                    e.getString("date"),
                                    e.getString("time"),
                                    e.getString("name"),
                                    e.getString("address"),
                                    e.optString("phone", ""),
                                    e.optString("details", ""),
                                    e.optString("notes", "")
                            ));
                        }
                    }
                    break;
                }
            }

            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Błąd ładowania wydarzeń", Toast.LENGTH_SHORT).show();
        }
    }

    private void filterEventsByDate(LocalDate date) {
        List<EventItem> filtered = new ArrayList<>();
        for (EventItem item : fullEventList) {
            if (item.getDate().equals(date.toString())) {
                filtered.add(item);
            }
        }
        adapter = new EventAdapter(filtered, currentLogin);
        recyclerView.setAdapter(adapter);
    }

    private void showAllEventsGrouped() {
        Map<String, List<EventItem>> grouped = new TreeMap<>();

        for (EventItem item : fullEventList) {
            String date = item.getDate();
            if (!grouped.containsKey(date)) {
                grouped.put(date, new ArrayList<>());
            }
            grouped.get(date).add(item);
        }

        List<EventItem> groupedWithHeaders = new ArrayList<>();
        for (String date : grouped.keySet()) {
            groupedWithHeaders.add(new EventItem(date, null, null, null, null, null, null));
            groupedWithHeaders.addAll(grouped.get(date));
        }

        adapter = new EventAdapter(groupedWithHeaders, currentLogin);
        recyclerView.setAdapter(adapter);
    }

    private LocalDate convertToLocalDate(java.util.Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}