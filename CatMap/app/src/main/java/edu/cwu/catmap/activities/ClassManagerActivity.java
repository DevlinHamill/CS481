package edu.cwu.catmap.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import edu.cwu.catmap.R;
import edu.cwu.catmap.core.ClassItem;
import edu.cwu.catmap.databinding.ActivityClassManagerBinding;
import edu.cwu.catmap.utilities.Constants;
import edu.cwu.catmap.utilities.FirestoreTraceback;
import edu.cwu.catmap.utilities.FirestoreUtility;
import edu.cwu.catmap.utilities.ToastHelper;

public class ClassManagerActivity extends AppCompatActivity {

    private ActivityClassManagerBinding binding;
    private ClassAdapter classAdapter;
    private RecyclerView recyclerView;
    private List<ClassItem> classItemList;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityClassManagerBinding.inflate(getLayoutInflater());
        context = this;
        recyclerView = binding.recyclerView;
        setContentView(binding.getRoot());
        EdgeToEdge.enable(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setListeners();
    }

    protected void onResume() {
        super.onResume();
        initializeRecyclerView();
    }

    private void initializeRecyclerView() {
        FirestoreUtility.getInstance().getClasses(new FirestoreTraceback() {
            @Override
            public void success(String message, Object data) {
                classItemList = (ArrayList<ClassItem>) data;

                classAdapter = new ClassAdapter(classItemList, new ClassAdapter.OnClassClickListener() {
                    @Override
                    public void onClassSelected(int position) {
                        toggleButtonState();
                    }
                });

                recyclerView.setLayoutManager(new LinearLayoutManager(context));
                recyclerView.setAdapter(classAdapter);
            }

            @Override
            public void failure(String message) {
                ToastHelper.showToast(context, message);
            }
        });
    }

    private void setListeners() {
        binding.buttonDeleteClass.setOnClickListener(v -> {
            showDeleteDialog();
        });
        binding.buttonAddEvent.setOnClickListener(v -> {
            showDatePickerDialog(true);
        });
        binding.buttonAddClass.setOnClickListener(v -> {
            showDatePickerDialog(false);
        });
    }

    /**
     * Launch an alertDialog to make sure they really want to delete the class.
     */
    private void showDeleteDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Class")
                .setMessage("Deleting a class will delete all it's events! Are you sure?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    FirestoreUtility.getInstance().removeClass(classAdapter.getSelectedClassItem().getName(), new FirestoreTraceback() {
                        @Override
                        public void success(String message) {
                            ToastHelper.showToast(context, message);
                        }

                        @Override
                        public void failure(String message) {
                            ToastHelper.showToast(context, message);
                        }
                    });
                    classAdapter.removeSelectedItem();
                    disableButtons();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void showDatePickerDialog(boolean isExistingClass) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    //unused callback method, we will attach our own when the
                    //confirmed button is clicked, set later on
                },
                year, month, day
        );

        DatePicker datePicker = datePickerDialog.getDatePicker();

        datePickerDialog.setTitle("Select Class Event Date");

        datePickerDialog.setButton(DatePickerDialog.BUTTON_POSITIVE, "Select", ((dialog, which) -> {
            int selectedYear = datePicker.getYear();
            int selectedMonth = datePicker.getMonth() + 1;
            int selectedDay = datePicker.getDayOfMonth();

            String monthString = selectedMonth < 10 ? "0" + selectedMonth : String.valueOf(selectedMonth);
            String dateString = ( monthString + "/" + selectedDay + "/" + selectedYear);

            Log.i("Class Manager", "Date picker string: " + dateString);

            Intent intent = getNewEventIntent(isExistingClass, dateString);

            startActivity(intent);
        }));

        datePickerDialog.show();
    }

    @NonNull
    private Intent getNewEventIntent(boolean isExistingClass, String dateString) {
        Intent intent = new Intent(this, NewEvent.class);

        intent.putExtra(Constants.KEY_NEW_EVENT_TYPE, Constants.VALUE_EVENT_TYPE_CLASS);
        intent.putExtra(Constants.KEY_NEW_EVENT_SELECTED_DATE, dateString);
        intent.putExtra(Constants.KEY_NEW_EVENT_IS_EXISTING_CLASS, isExistingClass);

        if(isExistingClass) {
            intent.putExtra(Constants.KEY_NEW_EVENT_CLASS_NAME, classAdapter.getSelectedClassItem().getName());
            intent.putExtra(Constants.KEY_NEW_EVENT_CLASS_COLOR, classAdapter.getSelectedClassItem().getColor());
        }
        return intent;
    }

    private void disableButtons() {
        binding.buttonDeleteClass.setEnabled(false);
        binding.buttonAddEvent.setEnabled(false);
    }

    public void toggleButtonState() {
        if (!classItemList.isEmpty()) {
            binding.buttonDeleteClass.setEnabled(true);
            binding.buttonAddEvent.setEnabled(true);
        } else {
            disableButtons();
        }
    }

    public static class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.ClassViewHolder> {
        private List<ClassItem> classList;
        private int selectedPosition = RecyclerView.NO_POSITION;
        private OnClassClickListener listener;

        public ClassAdapter(List<ClassItem> classList, OnClassClickListener listener) {
            this.classList = classList;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ClassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.class_manager_class, parent, false);
            return new ClassViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ClassViewHolder holder, int position) {
            ClassItem classItem = classList.get(position);
            holder.className.setText(classItem.getName());

            //set the color circle background's color to match with the chosen color
            ((GradientDrawable) holder.colorCircle.getBackground()).setColor(classItem.getColor());

            Context itemContext = holder.itemView.getContext();

            // Fetch the colors from theme using TypedValue
            TypedValue typedValue = new TypedValue();
            itemContext.getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnPrimary, typedValue, true);
            int colorOnPrimary = typedValue.data;

            itemContext.getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnBackground, typedValue, true);
            int colorOnBackground = typedValue.data;

            //if this class is selected, reflect that in its background and text color
            if(selectedPosition == position) {
                holder.itemView.setBackgroundResource(R.drawable.background_rectangle_selected);
                holder.className.setTextColor(colorOnPrimary);
            }
            //other wise use defaults
            else {
                holder.itemView.setBackgroundResource(R.drawable.background_rectangle_unselected);
                holder.className.setTextColor(colorOnBackground);
            }

            //when this class is clicked, change selected position and update the backgrounds of this and
            //the previously selected classes.
            holder.itemView.setOnClickListener(v -> {
                // Get the current position on click to ensure it's up-to-date
                int clickedPosition = holder.getAdapterPosition();
                if (clickedPosition != RecyclerView.NO_POSITION) {  // Check if position is valid
                    int previousPosition = selectedPosition;
                    selectedPosition = clickedPosition;
                    notifyItemChanged(previousPosition);
                    notifyItemChanged(selectedPosition);
                    listener.onClassSelected(clickedPosition);
                }
            });

        }

        @Override
        public int getItemCount() {
            return classList.size();
        }

        public void removeSelectedItem() {
            if(selectedPosition != RecyclerView.NO_POSITION) {
                classList.remove(selectedPosition);
                notifyItemRemoved(selectedPosition);
                notifyItemRangeChanged(selectedPosition, classList.size());

                selectedPosition = RecyclerView.NO_POSITION;
            }
        }

        public int getSelectedPosition() {
            return selectedPosition;
        }

        public ClassItem getSelectedClassItem() {
            return classList.get(selectedPosition);
        }

        public static class ClassViewHolder extends RecyclerView.ViewHolder {
            TextView className;
            View colorCircle;
            public ClassViewHolder(View itemView) {
                super(itemView);

                className = itemView.findViewById(R.id.className);
                colorCircle = itemView.findViewById(R.id.colorCircle);
            }
        }

        public interface OnClassClickListener {
            void onClassSelected(int position);
        }
    }
}