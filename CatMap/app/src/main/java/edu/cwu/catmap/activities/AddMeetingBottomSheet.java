package edu.cwu.catmap.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import edu.cwu.catmap.R;

public class AddMeetingBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_SELECTED_DATE = "SELECTED_DATE"; // Key for passing date

    public static AddMeetingBottomSheet newInstance(String selectedDate) {
        AddMeetingBottomSheet fragment = new AddMeetingBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_SELECTED_DATE, selectedDate);
        fragment.setArguments(args);
        return fragment;
    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button addToClassButton = view.findViewById(R.id.AddtoClassbutton);
        Button addToEventButton = view.findViewById(R.id.AddtoEventbutton);

        // Retrieve the selected date from arguments
        String selectedDate = getArguments() != null ? getArguments().getString(ARG_SELECTED_DATE) : null;

        // Set up click listeners to navigate to NewEvent
        addToClassButton.setOnClickListener(v -> openNewEvent("Class", selectedDate));
        addToEventButton.setOnClickListener(v -> openNewEvent("Event", selectedDate));
    }


    /**
     * Opens NewEvent activity directly with the selected type (Class/Event).
     */
    private void openNewEvent(String type, String selectedDate) {
        if (selectedDate == null) {
            return; // Prevents starting activity with a null value
        }
        if (type == "Class"){
            Intent intent = new Intent(getActivity(), ClassManagerActivity.class);
            intent.putExtra("header", type);
            intent.putExtra("SELECTED_DATE", selectedDate);
            startActivity(intent);
        }else {

            Intent intent = new Intent(getActivity(), NewEvent.class);
            intent.putExtra("header", type);
            intent.putExtra("SELECTED_DATE", selectedDate); // Pass date correctly
            startActivity(intent);
        }

        dismiss(); // Close the bottom sheet
    }



}
