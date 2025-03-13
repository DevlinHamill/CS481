package edu.cwu.catmap.activities;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import edu.cwu.catmap.R;

public class AddMeetingBottomSheet extends BottomSheetDialogFragment {

    private MaterialSwitch switchRepeat;
    private LinearLayout repeatOptionsContainer;
    private TextView tvEndDate;
    private Set<String> selectedDays = new HashSet<>();
    private MaterialButton[] toggleButtons;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_add_meeting, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Dismiss bottom sheet when clicking outside
        if (getDialog() != null) {
            getDialog().setCanceledOnTouchOutside(true);
        }

        // Initialize UI elements
        switchRepeat = view.findViewById(R.id.switchRepeat);
        repeatOptionsContainer = view.findViewById(R.id.repeatOptionsContainer);
        tvEndDate = view.findViewById(R.id.tvEndDate);

        // Day toggle buttons
        toggleButtons = new MaterialButton[]{
                view.findViewById(R.id.btnMon),
                view.findViewById(R.id.btnTue),
                view.findViewById(R.id.btnWed),
                view.findViewById(R.id.btnThu),
                view.findViewById(R.id.btnFri),
                view.findViewById(R.id.btnSat),
                view.findViewById(R.id.btnSun)
        };

        // Show or hide repeat options based on switch state
        switchRepeat.setOnCheckedChangeListener((buttonView, isChecked) -> {
            repeatOptionsContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        // Handle day selection (toggle background color)
//        for (MaterialButton button : toggleButtons) {
//            button.setOnClickListener(v -> toggleDaySelection(button));
//        }

        // Handle End Date selection
        tvEndDate.setOnClickListener(v -> showDatePicker());
    }

//    // Toggle day selection
//    private void toggleDaySelection(MaterialButton button) {
//        String day = button.getText().toString();
//        if (selectedDays.contains(day)) {
//            selectedDays.remove(day);
//            button.setBackgroundColor(Color.TRANSPARENT); // Deselect
//        } else {
//            selectedDays.add(day);
//            button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.teal_200)); // Selected color
//        }
//    }

    // Show Date Picker for End Date selection
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            String selectedDate = (month + 1) + "/" + dayOfMonth + "/" + year;
            tvEndDate.setText(selectedDate);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePicker.show();
    }
}
