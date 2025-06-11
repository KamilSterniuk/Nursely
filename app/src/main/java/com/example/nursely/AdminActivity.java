package com.example.nursely;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ExpandableListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.util.*;

public class AdminActivity extends AppCompatActivity {

    ExpandableListView expandableListView;
    List<String> nurseList;
    HashMap<String, List<JSONObject>> visitMap;
    AdminExpandableListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        expandableListView = findViewById(R.id.expandableListView);
        nurseList = new ArrayList<>();
        visitMap = new HashMap<>();

        loadVisits();

        adapter = new AdminExpandableListAdapter(this, nurseList, visitMap);
        expandableListView.setAdapter(adapter);

        findViewById(R.id.addGlobalFab).setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, AddVisitActivity.class);
            // NIE przekazujemy loginu – w AddVisitActivity pokaże się dropdown do wyboru pielęgniarki
            startActivity(intent);
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadVisits();
        adapter = new AdminExpandableListAdapter(this, nurseList, visitMap);
        expandableListView.setAdapter(adapter);
    }


    private void loadVisits() {
        try {
            nurseList.clear();
            visitMap.clear();

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
                if (user.getString("userType").equals("nurse")) {
                    String nurseLogin = user.getString("login");
                    nurseList.add(nurseLogin);
                    List<JSONObject> visits = new ArrayList<>();

                    JSONArray events = user.optJSONArray("events");
                    if (events != null) {
                        for (int j = 0; j < events.length(); j++) {
                            JSONObject e = events.getJSONObject(j);
                            visits.add(e);
                        }

                        // Sortuj po dacie i godzinie
                        visits.sort(Comparator.comparing(o ->
                                o.optString("date", "") + " " + o.optString("time", ""))
                        );
                    }

                    visitMap.put(nurseLogin, visits);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Błąd podczas ładowania danych", Toast.LENGTH_SHORT).show();
        }
    }



}
