package com.example.nursely;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    List<EventItem> eventList;
    String login;

    public EventAdapter(List<EventItem> eventList, String login) {
        this.eventList = eventList;
        this.login = login;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        EventItem item = eventList.get(position);

        if (item.getTime() == null) {          // nagłówek daty
            holder.time.setText(item.getDate());
            holder.time.setTextSize(18);
            holder.name.setVisibility(View.GONE);
            holder.address.setVisibility(View.GONE);
            holder.itemView.setAlpha(1f);      // nagłówki zawsze pełne
        } else {                               // zwykła wizyta
            holder.time.setText(item.getTime());
            holder.name.setText(item.getPatientName());
            holder.address.setText(item.getAddress());
            holder.time.setTextSize(16);
            holder.name.setVisibility(View.VISIBLE);
            holder.address.setVisibility(View.VISIBLE);

            // === WYSZARZANIE PRZESZŁYCH WIZYT ===
            try {
                String dateTime = item.getDate() + " " + item.getTime(); // "2025-05-06 08:30"
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                Date visitDate = sdf.parse(dateTime);
                if (visitDate != null && visitDate.before(new Date())) {
                    holder.itemView.setAlpha(0.5f);     // wizyta w przeszłości
                } else {
                    holder.itemView.setAlpha(1f);       // przyszła lub dzisiejsza
                }
            } catch (Exception e) {
                holder.itemView.setAlpha(1f);
            }

            // kliknięcie → szczegóły wizyty pielęgniarki
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), EventDetailActivity.class);
                intent.putExtra("login", login);
                intent.putExtra("date", item.getDate());
                intent.putExtra("time", item.getTime());
                v.getContext().startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView time, name, address;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            time    = itemView.findViewById(R.id.eventTime);
            name    = itemView.findViewById(R.id.patientName);
            address = itemView.findViewById(R.id.patientAddress);
        }
    }
}
