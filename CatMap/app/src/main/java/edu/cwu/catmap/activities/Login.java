package edu.cwu.catmap.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import edu.cwu.catmap.R;
import edu.cwu.catmap.databinding.ActivityLoginBinding;
import edu.cwu.catmap.manager.UserManager;

public class Login extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private UserManager userManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        userManager = UserManager.getInstance();
        setListeners();
    }

    private void setListeners() {
        binding.newAccount.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), Register.class)));

        binding.loginButton.setOnClickListener(v -> {
            if (isValidateSignInDetails()) {
                userManager.signin(
                        binding.emailInput.getText().toString(),
                        binding.passwordInput.getText().toString(),
                        task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot userDocument = task.getResult();
                                if (userDocument != null && userDocument.exists()) {
                                    showToast("Login successful!");
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                } else {
                                    showToast("User data not found");
                                }
                            } else {
                                showToast("Failed to retrieve user data");
                            }
                        }
                );
            }
        });

        binding.forgotPassword.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), ForgotPassword.class)));
    }

    /**
     * Helper function to display toasts
     *
     * @param message a string to display as a Toast
     */
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Helper function used to hide or display the sign in button and progress bar
     *
     * @param isLoading signals if it's a state where the user clicks the button or the UI finished
     *                  loading
     */
    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.loginButton.setVisibility(View.INVISIBLE);
            //binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.loginButton.setVisibility(View.VISIBLE);
            //binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * This function retrieves the text in the e-mail and password TextEdit Views and checks if
     * the email is valid (using a pattern checker) or exists, and if the password exists
     *
     * @return signals if the user input is valid or not
     */
    private boolean isValidateSignInDetails() {
        if (binding.emailInput.getText().toString().trim().isEmpty()) {
            showToast("Please enter your e-mail");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.emailInput.getText().toString()).matches()) {
            showToast("Please enter a valid e-mail");
            return false;
        } else if (binding.passwordInput.getText().toString().trim().isEmpty()) {
            showToast("Please enter your password");
            return false;
        } else {
            return true;
        }
    }
}