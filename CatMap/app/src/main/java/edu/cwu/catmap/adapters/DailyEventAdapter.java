package edu.cwu.catmap.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.cwu.catmap.R;
import edu.cwu.catmap.activities.ScheduleDetails;
import edu.cwu.catmap.core.ScheduleListItem;

public class DailyEventAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_EVENT = 1;

    private List<ScheduleListItem> items;

    public DailyEventAdapter(List<ScheduleListItem> items) {
        this.items = items;
    }

    public void updateData(List<ScheduleListItem> newItems) {
        this.items.clear();
        this.items.addAll(newItems);
        notifyDataSetChanged();
    }
    @Override
    public int getItemViewType(int position) {
        ScheduleListItem item = items.get(position);
        if (item instanceof ScheduleListItem.Event) {
            return TYPE_EVENT;
        }
        throw new IllegalArgumentException("Unsupported item type: " + item.getClass());
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_EVENT) {
            View view = inflater.inflate(R.layout.daily_event, parent, false); // Use the new item_event.xml layout
            return new EventViewHolder(view);
        }
        throw new IllegalArgumentException("Unsupported view type: " + viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ScheduleListItem item = items.get(position);
        if (holder instanceof EventViewHolder) {
            ((EventViewHolder) holder).bind((ScheduleListItem.Event) item);

            // Set click listener for the event
            holder.itemView.setOnClickListener(v -> {
                Context context = v.getContext();
                Intent intent = new Intent(context, ScheduleDetails.class);
                intent.putExtra("Map", ((ScheduleListItem.Event) item).getMap());
                intent.putExtra("Event_Title", ((ScheduleListItem.Event) item).getTitle());
                context.startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    //ViewHolder for events
    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView timeLabel;
        Button eventButton;

        EventViewHolder(View itemView) {
            super(itemView);
            timeLabel = itemView.findViewById(R.id.timeLabel);
            eventButton = itemView.findViewById(R.id.eventButton);
        }

        void bind(ScheduleListItem.Event item) {
            timeLabel.setText(item.getTime());
            eventButton.setText(item.getTitle());

            //extract the colorPreference
            String colorPreference = item.getMap().get("Color_Preference");

            //set background color
            if (colorPreference != null && !colorPreference.isEmpty()) {
                try {
                    int color = Integer.parseInt(colorPreference);
                    eventButton.setBackgroundColor(color);
                } catch (IllegalArgumentException e) {
                    eventButton.setBackgroundColor(Color.GRAY);
                }
            } else {
                eventButton.setBackgroundColor(Color.GRAY);
            }

            if (item.getTitle().isEmpty()) {
                eventButton.setVisibility(View.INVISIBLE);
            } else {
                eventButton.setVisibility(View.VISIBLE);
            }
        }
    }
}