package com.example.nursely;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class AdminExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> nurseList;
    private HashMap<String, List<JSONObject>> visitMap;

    public AdminExpandableListAdapter(Context context, List<String> nurseList, HashMap<String, List<JSONObject>> visitMap) {
        this.context = context;
        this.nurseList = nurseList;
        this.visitMap = visitMap;
    }

    @Override
    public int getGroupCount() {
        return nurseList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return visitMap.get(nurseList.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return nurseList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return visitMap.get(nurseList.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }


    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.group_nurse_header, parent, false);
        }

        String nurseLogin = nurseList.get(groupPosition);
        ((TextView) convertView.findViewById(R.id.nurseNameText))
                .setText("Pielęgniarka: " + nurseLogin);

        // plusik
        convertView.findViewById(R.id.addVisitIcon).setOnClickListener(v -> {
            Intent intent = new Intent(context, AddVisitActivity.class);
            intent.putExtra("login", nurseLogin);   // przekazujemy konkretną pielęgniarkę
            context.startActivity(intent);
        });

        return convertView;
    }


    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        JSONObject event = (JSONObject) getChild(groupPosition, childPosition);
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.item_admin_visit, parent, false);
        }

        try {
            String date = event.getString("date");
            String time = event.getString("time");
            String name = event.getString("name");
            String details = event.optString("details", "");

            String fullTime = date + " " + time;

            TextView timeView = convertView.findViewById(R.id.visitTimeDate);
            TextView patientView = convertView.findViewById(R.id.visitPatient);
            TextView detailsView = convertView.findViewById(R.id.visitDetails);

            timeView.setText(fullTime);
            patientView.setText(name);
            detailsView.setText(details);

            // WYSZARZANIE jeśli wizyta się już odbyła
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                Date visitDate = sdf.parse(fullTime);
                Date now = new Date();

                if (visitDate != null && visitDate.before(now)) {
                    // Wizyta w przeszłości – wyszarz
                    convertView.setAlpha(0.5f);
                } else {
                    // Wizyta w przyszłości – pełna widoczność
                    convertView.setAlpha(1.0f);
                }
            } catch (Exception e) {
                e.printStackTrace();
                convertView.setAlpha(1.0f);
            }

            convertView.setOnClickListener(v -> {
                Intent intent = new Intent(context, AdminVisitDetailActivity.class);
                intent.putExtra("login", nurseList.get(groupPosition));
                intent.putExtra("event_date", date);
                intent.putExtra("event_time", time);
                intent.putExtra("event_name", name);
                intent.putExtra("event_address", event.optString("address", ""));
                intent.putExtra("event_phone", event.optString("phone", ""));
                intent.putExtra("event_details", details);
                context.startActivity(intent);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

        return convertView;
    }


    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
