package edu.cwu.catmap.activities;

import android.content.Context;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;

import edu.cwu.catmap.R;
import edu.cwu.catmap.core.Schedule;
import edu.cwu.catmap.core.User;
import edu.cwu.catmap.databinding.ActivityMainBinding;
import edu.cwu.catmap.managers.UserManager;
import edu.cwu.catmap.utilities.FirestoreUtility;
import edu.cwu.catmap.utilities.ToastHelper;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Context context = this;

        binding.signUpButton.setOnClickListener(view -> {
            String email = binding.signupEmailEditText.getText().toString().trim();
            String password = binding.signupPasswordEditText.getText().toString();
            String name = binding.signupNameEditText.getText().toString().trim();
            if(!Patterns.EMAIL_ADDRESS.matcher(email).matches() || password.isEmpty() ||name.isEmpty()) {
                ToastHelper.showToast(this, "Invalid sign-up email or password, try again!");
            }
            else {
                hideKeyboard();
                UserManager.getInstance().signUp(email, password, name, null, new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            ToastHelper.showToast(context, "Account Created Successfully");
                        }

                        else {
                            ToastHelper.showToast(context, "Failed to sign up user");
                        }
                    }
                });
            }
        });

        binding.signinButton.setOnClickListener(view -> {
            String email = binding.signinEmailEditText.getText().toString().trim();
            String password = binding.signinPasswordEditText.getText().toString();
            if(!Patterns.EMAIL_ADDRESS.matcher(email).matches() || password.isEmpty()) {
                ToastHelper.showToast(this, "Invalid sign-in email, password, or name, try again!");
            }
            else {
                hideKeyboard();
                UserManager.getInstance().signin(email, password, new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            ToastHelper.showToast(context, "signed in Successfully");
                        }

                        else {
                            ToastHelper.showToast(context, "Failed to sign in user");
                        }
                    }
                });
            }
        });

        binding.storeScheduleButton.setOnClickListener(view -> {
            UserManager.getInstance().getCurrentUser().setSchedule(new Schedule(LocalDate.now(), LocalDate.now().plusWeeks(4)));
            FirestoreUtility.getInstance().storeSchedule(UserManager.getInstance().getCurrentFirebaseUser(), UserManager.getInstance().getCurrentUser().getSchedule(), new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    ToastHelper.showToast(context, "Sent Schedule");
                }
            });

            FirestoreUtility.getInstance().getSchedule(UserManager.getInstance().getCurrentFirebaseUser(), new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        ToastHelper.showToast(context, "Successfully retrieved schedule");
                    }
                    else {
                        ToastHelper.showToast(context, "Failed to retrieve schedule");
                    }
                }
            });
        });

    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            // Get the InputMethodManager system service
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                // Hide the keyboard
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }
}