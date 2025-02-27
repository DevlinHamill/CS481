package edu.cwu.catmap.adapters;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import edu.cwu.catmap.R;
import edu.cwu.catmap.core.FavoriteLocationsListItem;
import edu.cwu.catmap.core.Schedule;
import edu.cwu.catmap.core.ScheduleListItem;

public class SchedulerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {
    private static final int TYPE_SECTION_HEADER = 0;
    private static final int TYPE_EVENT = 1;

    private List<ScheduleListItem> items, filteredItems;

    public SchedulerAdapter(List<ScheduleListItem> items) {
        this.items = items;
        filteredItems = new ArrayList<>(items);
    }

    public int getItemViewType(int position) {
        ScheduleListItem item = filteredItems.get(position);
        int type = -1;
        if(item instanceof ScheduleListItem.SectionHeader) {
            type = TYPE_SECTION_HEADER;
        }
        else if(item instanceof ScheduleListItem.Event) {
            type = TYPE_EVENT;
        }
        else {
            Log.e("FavoriteLocationsAdapter", "item of wrong type in items. type of " + item.getClass());
        }

        return type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        RecyclerView.ViewHolder viewHolder = null;
        View view = null;

        if (viewType == TYPE_SECTION_HEADER) {
            view = inflater.inflate(R.layout.item_container_schedule, parent, false);
            viewHolder = new SchedulerAdapter.SectionHeaderViewHolder(view);
        }
        else if (viewType == TYPE_EVENT) {
            view = inflater.inflate(R.layout.item_container_event, parent, false);
            viewHolder = new SchedulerAdapter.EventViewHolder(view);
        }
        else {
            Log.e("FavoriteLocationsAdapter", "viewType cannot be determined, give type of " + viewType);
        }

        assert viewHolder != null;
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ScheduleListItem item = filteredItems.get(position);

        if (holder instanceof SectionHeaderViewHolder) {
            ((SchedulerAdapter.SectionHeaderViewHolder) holder).bind((ScheduleListItem.SectionHeader) item);
        }
        else if (holder instanceof EventViewHolder) {
            ((EventViewHolder) holder).bind((ScheduleListItem.Event) item);
        }

        else {
            Log.e("FavoriteLocationsAdapter", "holder type cannot be determined. type of " + holder.getClass());
        }
    }

    @Override
    public int getItemCount() {
        return filteredItems == null ? 0 : filteredItems.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<ScheduleListItem> filteredList = new ArrayList<>();

                if (TextUtils.isEmpty(constraint)) {
                    filteredList.addAll(items);
                }
                else {
                    String filterPattern = constraint.toString().toLowerCase().trim();

                    for (ScheduleListItem item : items) {
                        if (item instanceof ScheduleListItem.SectionHeader) {
                            filteredList.add(item);
                        }
                        else if (item instanceof ScheduleListItem.Event) {
                            filteredList.add(item);
                        }

                    }
                }

                FilterResults results = new FilterResults();
                results.values = filteredList;
                results.count = filteredList.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredItems.clear();
                filteredItems.addAll((List<ScheduleListItem>) results.values);
                notifyDataSetChanged();
            }
        };
    }

    // ViewHolder for section headers
    static class SectionHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView sectionTitle;

        SectionHeaderViewHolder(View itemView) {
            super(itemView);
            sectionTitle = itemView.findViewById(R.id.headerText);
        }

        void bind(ScheduleListItem.SectionHeader item) {
            sectionTitle.setText(item.getDate());
        }


    }

    // ViewHolder for favorite locations (has color indicator)
    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView EventName;
        TextView Time;

        EventViewHolder(View itemView) {
            super(itemView);
            EventName = itemView.findViewById(R.id.event_name_left);
            Time = itemView.findViewById(R.id.time_right);
        }

        void bind(ScheduleListItem.Event item) {
            EventName.setText(item.getTitle());
            Time.setText(item.getTime());
        }
    }

}
