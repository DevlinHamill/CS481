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
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import edu.cwu.catmap.R;
import edu.cwu.catmap.databinding.ActivityLoginBinding;
import edu.cwu.catmap.manager.UserManager;

/**
 * Handles user login authentication, including validation, error handling,
 * and navigation to the main activity after successful login.
 */
public class Login extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private UserManager userManager;

    /**
     * Initializes the Login activity, setting up UI components and authentication logic.
     *
     * @param savedInstanceState Previous saved state, if available.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        userManager = UserManager.getInstance();
        setListeners();
    }

    /**
     * Sets up click listeners for login, account creation, and password recovery actions.
     */
    private void setListeners() {
        binding.newAccount.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), Register.class)));

        binding.loginButton.setOnClickListener(v -> {
            if (isValidateSignInDetails()) {
                // Show loading bar & disable button
                loading(true);
                // Clear previous errors
                binding.passwordLayout.setError(null);
                binding.emailLayout.setError(null);

                String email = binding.emailInput.getText().toString().trim();
                String password = binding.passwordInput.getText().toString().trim();

                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            loading(false); // Hide loading bar when done
                            if (task.isSuccessful()) {
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                if (user != null) {
                                    showToast("Login successful!");
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                } else {
                                    showPasswordError("User data not found");
                                }
                            } else {
                                // Handle authentication failure
                                handleFirebaseAuthError(task.getException());
                            }
                        });
            }
        });

        binding.forgotPassword.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), ForgotPassword.class)));
    }


    /**
     * Handles Firebase authentication errors and displays appropriate messages.
     *
     * @param exception The authentication exception encountered.
     */
    private void handleFirebaseAuthError(Exception exception) {
        if (exception == null) {
            showPasswordError("Authentication failed. Try again.");
            return;
        }

        String errorMessage = "Incorrect email or password. Please try again.";

        if (exception instanceof FirebaseAuthInvalidUserException) {
            errorMessage = "No account found with this email.";
            binding.emailLayout.setError(errorMessage); // Show error in email field
            return;
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            errorMessage = "Incorrect email or password. Please try again.";
        } else if (exception instanceof FirebaseAuthUserCollisionException) {
            errorMessage = "This email is already in use.";
        } else if (exception instanceof FirebaseAuthWeakPasswordException) {
            errorMessage = "Your password is too weak.";
        }

        showPasswordError(errorMessage);
    }

    /**
     * Displays an error message inside the password input field.
     *
     * @param message The error message to display.
     */
    private void showPasswordError(String message) {
        binding.passwordLayout.setError(message);
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
        String email = binding.emailInput.getText().toString().trim();
        String password = binding.passwordInput.getText().toString().trim();

        // Validate Email
        if (email.isEmpty()) {
            binding.emailLayout.setError("Please enter your e-mail");
            binding.emailInput.requestFocus();
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailLayout.setError("Please enter a valid e-mail");
            binding.emailInput.requestFocus();
            return false;
        } else {
            binding.emailLayout.setError(null); // Remove error when valid
        }

        // Validate Password
        if (password.isEmpty()) {
            binding.passwordLayout.setError("Please enter your password");
            binding.passwordInput.requestFocus();
            return false;
        } else {
            binding.passwordLayout.setError(null); // Remove error when valid
        }

        return true;
    }

}