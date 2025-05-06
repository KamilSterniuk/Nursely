package com.example.nursely;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    List<EventItem> eventList;

    public EventAdapter(List<EventItem> eventList) {
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        EventItem item = eventList.get(position);

        if (item.getTime() == null) {
            // Nagłówek daty
            holder.time.setText(item.getDate());
            holder.time.setTextSize(18);
            holder.name.setVisibility(View.GONE);
            holder.address.setVisibility(View.GONE);
        } else {
            holder.time.setText(item.getTime());
            holder.name.setText(item.getPatientName());
            holder.address.setText(item.getAddress());
            holder.time.setTextSize(16);
            holder.name.setVisibility(View.VISIBLE);
            holder.address.setVisibility(View.VISIBLE);
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
            time = itemView.findViewById(R.id.eventTime);
            name = itemView.findViewById(R.id.patientName);
            address = itemView.findViewById(R.id.patientAddress);
        }
    }
}
