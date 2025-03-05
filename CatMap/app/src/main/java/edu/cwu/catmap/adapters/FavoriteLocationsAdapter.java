package edu.cwu.catmap.adapters;
import android.graphics.drawable.GradientDrawable;
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


public class FavoriteLocationsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {
    private static final int TYPE_SECTION_HEADER = 0;
    private static final int TYPE_FAVORITE_LOCATION = 1;
    private static final int TYPE_LOCATION = 2;

    private List<FavoriteLocationsListItem> items, filteredItems;

    public FavoriteLocationsAdapter(List<FavoriteLocationsListItem> items) {
        this.items = items;
        filteredItems = new ArrayList<>(items);
    }

    public int getItemViewType(int position) {
        FavoriteLocationsListItem item = filteredItems.get(position);
        int type = -1;
        if(item instanceof FavoriteLocationsListItem.SectionHeader) {
            type = TYPE_SECTION_HEADER;
        }
        else if(item instanceof FavoriteLocationsListItem.FavoriteLocation) {
            type = TYPE_FAVORITE_LOCATION;
        }
        else if(item instanceof FavoriteLocationsListItem.Location) {
            type = TYPE_LOCATION;
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
            view = inflater.inflate(R.layout.favorite_locations_section_header, parent, false);
            viewHolder = new SectionHeaderViewHolder(view);
        }
        else if (viewType == TYPE_FAVORITE_LOCATION) {
            view = inflater.inflate(R.layout.favorite_locations_favorite_location, parent, false);
            viewHolder = new FavoriteLocationViewHolder(view);
        }
        else if (viewType == TYPE_LOCATION) {
            view = inflater.inflate(R.layout.favorite_locations_location, parent, false);
            viewHolder = new LocationViewHolder(view);
        }
        else {
            Log.e("FavoriteLocationsAdapter", "viewType cannot be determined, give type of " + viewType);
        }

        assert viewHolder != null;
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        FavoriteLocationsListItem item = filteredItems.get(position);

        if (holder instanceof SectionHeaderViewHolder) {
            ((SectionHeaderViewHolder) holder).bind((FavoriteLocationsListItem.SectionHeader) item);
        }
        else if (holder instanceof FavoriteLocationViewHolder) {
            ((FavoriteLocationViewHolder) holder).bind((FavoriteLocationsListItem.FavoriteLocation) item);
        }
        else if (holder instanceof LocationViewHolder) {
            ((LocationViewHolder) holder).bind((FavoriteLocationsListItem.Location) item);
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
                List<FavoriteLocationsListItem> filteredList = new ArrayList<>();

                if (TextUtils.isEmpty(constraint)) {
                    filteredList.addAll(items);
                }
                else {
                    String filterPattern = constraint.toString().toLowerCase().trim();

                    for (FavoriteLocationsListItem item : items) {
                        if (item instanceof FavoriteLocationsListItem.SectionHeader) {
                            filteredList.add(item);
                        }
                        else if (item instanceof FavoriteLocationsListItem.FavoriteLocation &&
                                ((FavoriteLocationsListItem.FavoriteLocation) item).getName().toLowerCase().contains(filterPattern)) {
                            filteredList.add(item);
                        }
                        else if (item instanceof FavoriteLocationsListItem.Location &&
                                ((FavoriteLocationsListItem.Location) item).getName().toLowerCase().contains(filterPattern)) {
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
                filteredItems.addAll((List<FavoriteLocationsListItem>) results.values);
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

        void bind(FavoriteLocationsListItem.SectionHeader item) {
            sectionTitle.setText(item.getTitle());
        }
    }

    // ViewHolder for favorite locations (has color indicator)
    static class FavoriteLocationViewHolder extends RecyclerView.ViewHolder {
        TextView locationName;
        View colorIndicator;

        FavoriteLocationViewHolder(View itemView) {
            super(itemView);
            locationName = itemView.findViewById(R.id.favoriteLocationName);
            colorIndicator = itemView.findViewById(R.id.colorIndicator);
        }

        void bind(FavoriteLocationsListItem.FavoriteLocation item) {
            locationName.setText(item.getName());

            // Set color dynamically
            GradientDrawable bgShape = (GradientDrawable) colorIndicator.getBackground();
            bgShape.setColor(item.getColor());
        }
    }

    //ViewHolder for regular locations
    static class LocationViewHolder extends RecyclerView.ViewHolder {
        TextView locationName;

        LocationViewHolder(View itemView) {
            super(itemView);
            locationName = itemView.findViewById(R.id.location_name);
        }

        void bind(FavoriteLocationsListItem.Location item) {
            locationName.setText(item.getName());
        }
    }
}
