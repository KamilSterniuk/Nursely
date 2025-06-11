package com.example.nursely;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.*;

public class AdminExpandableListAdapter extends BaseExpandableListAdapter {

    private final Context context;
    private final List<String> nurseLogins;
    private final HashMap<String, List<JSONObject>> visitMap;

    public AdminExpandableListAdapter(Context ctx,
                                      List<String> logins,
                                      HashMap<String, List<JSONObject>> map) {
        this.context = ctx;
        this.nurseLogins = logins;
        this.visitMap = map;
    }

    @Override public int  getGroupCount()             { return nurseLogins.size(); }
    @Override public int  getChildrenCount(int g)     { return visitMap.get(nurseLogins.get(g)).size(); }
    @Override public Object getGroup(int g)           { return nurseLogins.get(g); }
    @Override public Object getChild(int g,int c)     { return visitMap.get(nurseLogins.get(g)).get(c); }
    @Override public long getGroupId(int g)           { return g; }
    @Override public long getChildId(int g,int c)     { return c; }
    @Override public boolean hasStableIds()           { return false; }
    @Override public boolean isChildSelectable(int g,int c){ return true; }

    /* ---------- NAGŁÓWEK GRUPY ---------- */
    @Override
    public View getGroupView(int groupPos, boolean isExpanded,
                             View convertView, ViewGroup parent) {

        if (convertView == null)
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.group_nurse_header, parent, false);

        String login = nurseLogins.get(groupPos);
        TextView nameTv  = convertView.findViewById(R.id.nurseNameText);
        View phoneIcon   = convertView.findViewById(R.id.phoneIcon);
        View addIcon     = convertView.findViewById(R.id.addVisitIcon);

        // Szukamy imienia, nazwiska i telefonu
        String fullName  = login;
        final String[] phoneHolder = {""};

        try (BufferedReader br = new BufferedReader(
                new FileReader(new File(context.getFilesDir(),"users.json")))) {

            StringBuilder sb = new StringBuilder();
            String ln;
            while ((ln = br.readLine()) != null) sb.append(ln);

            JSONArray users = new JSONArray(sb.toString());
            for (int i=0;i<users.length();i++) {
                JSONObject u = users.getJSONObject(i);
                if (u.getString("login").equals(login)) {
                    fullName = u.optString("fullName",
                            (u.optString("name","") + " " + u.optString("surname","")).trim());
                    phoneHolder[0] = u.optString("phone", "");
                    break;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

        nameTv.setText("Pielęgniarka: " + fullName + " (" + login + ")");

        /* --- klik: telefon --- */
        phoneIcon.setOnClickListener(v -> {
            if (!phoneHolder[0].isEmpty()) {
                context.startActivity(new Intent(Intent.ACTION_DIAL,
                        Uri.parse("tel:" + phoneHolder[0])));
            } else {
                Toast.makeText(context, "Brak numeru telefonu", Toast.LENGTH_SHORT).show();
            }
        });

        /* --- klik: dodaj wizytę --- */
        addIcon.setOnClickListener(v -> {
            Intent i = new Intent(context, AddVisitActivity.class);
            i.putExtra("login", login);
            context.startActivity(i);
        });

        return convertView;
    }

    /* ---------- POJEDYNCZA WIZYTA ---------- */
    @Override
    public View getChildView(int g, int c, boolean isLast,
                             View convertView, ViewGroup parent) {

        if (convertView == null)
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_admin_visit, parent, false);

        JSONObject ev = visitMap.get(nurseLogins.get(g)).get(c);

        try {
            String date    = ev.getString("date");
            String time    = ev.getString("time");
            String patient = ev.getString("name");
            String details = ev.optString("details","");

            String dateTime = date + " " + time;

            ((TextView)convertView.findViewById(R.id.visitTimeDate)).setText(dateTime);
            ((TextView)convertView.findViewById(R.id.visitPatient)).setText(patient);
            ((TextView)convertView.findViewById(R.id.visitDetails)).setText(details);

            /* wyszarz przeszłe wizyty */
            try {
                Date visit = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                        .parse(dateTime);
                convertView.setAlpha(
                        (visit != null && visit.before(new Date())) ? 0.5f : 1f);
            } catch (Exception ignored){ convertView.setAlpha(1f); }

            /* klik: szczegóły */
            convertView.setOnClickListener(v -> {
                Intent i = new Intent(context, AdminVisitDetailActivity.class);
                i.putExtra("login", nurseLogins.get(g));
                i.putExtra("event_date", date);
                i.putExtra("event_time", time);
                i.putExtra("event_name", patient);
                i.putExtra("event_address", ev.optString("address",""));
                i.putExtra("event_phone",   ev.optString("phone",""));
                i.putExtra("event_details", details);
                context.startActivity(i);
            });

        } catch (Exception e) { e.printStackTrace(); }

        return convertView;
    }
}
