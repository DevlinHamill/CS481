package edu.cwu.catmap.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import edu.cwu.catmap.R;

/**
 * Handles password change functionality, including validation,
 * re-authentication, and updating the password in Firebase.
 */
public class ChangePassword extends AppCompatActivity {

    private TextInputEditText oldPasswordInput, newPasswordInput, confirmPasswordInput;
    private TextInputLayout oldPasswordLayout, newPasswordLayout, confirmPasswordLayout;
    private MaterialButton updatePasswordButton;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;

    /**
     * Initializes the ChangePassword activity, setting up UI components
     * and authentication logic.
     *
     * @param savedInstanceState Previous saved state, if available.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_password);

        // Set up system insets for proper layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.passwordScrollView), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase Authentication
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Find input fields
        oldPasswordInput = findViewById(R.id.currentPasswordInput);
        newPasswordInput = findViewById(R.id.newPasswordInput);
        confirmPasswordInput = findViewById(R.id.reenterPasswordInput);

        // Find input layouts for better error handling
        oldPasswordLayout = findViewById(R.id.currentPasswordInputLayout);
        newPasswordLayout = findViewById(R.id.newPasswordInputLayout);
        confirmPasswordLayout = findViewById(R.id.reenterPasswordInputLayout);

        updatePasswordButton = findViewById(R.id.updatePasswordButton);

        // Handle update password button click
        updatePasswordButton.setOnClickListener(v -> updatePassword());
    }

    /**
     * Handles password update process, including validation,
     * re-authentication, and updating the password in Firebase.
     */
    private void updatePassword() {
        String oldPassword = oldPasswordInput.getText().toString().trim();
        String newPassword = newPasswordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        // Validate input fields
        if (TextUtils.isEmpty(oldPassword)) {
            oldPasswordLayout.setError("Current password is required");
            oldPasswordInput.requestFocus();
            return;
        } else {
            oldPasswordLayout.setError(null);
        }

        if (TextUtils.isEmpty(newPassword)) {
            newPasswordLayout.setError("New password is required");
            newPasswordInput.requestFocus();
            return;
        } else if (!isStrongPassword(newPassword)) {
            newPasswordLayout.setError("Must be 6+ characters, include a number, uppercase letter & special character.");
            newPasswordInput.requestFocus();
            return;
        } else {
            newPasswordLayout.setError(null);
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordLayout.setError("Please confirm your new password");
            confirmPasswordInput.requestFocus();
            return;
        } else if (!newPassword.equals(confirmPassword)) {
            confirmPasswordLayout.setError("Passwords do not match");
            confirmPasswordInput.requestFocus();
            return;
        } else {
            confirmPasswordLayout.setError(null);
        }

        // Ensure the new password is different from the old password
        if (oldPassword.equals(newPassword)) {
            newPasswordLayout.setError("New password must be different from the current password");
            newPasswordInput.requestFocus();
            return;
        }

        // Validate password strength
        if (newPassword.length() < 6 || !newPassword.matches(".*\\d.*") || !newPassword.matches(".*[!@#$%^&*()_+=<>?/].*")) {
            newPasswordLayout.setError("Password must be at least 6 characters and include a number and special character");
            newPasswordInput.requestFocus();
            return;
        } else {
            newPasswordLayout.setError(null);
        }

        // Re-authenticate the user before changing the password
        String userEmail = user.getEmail();
        if (userEmail == null || !Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
            Toast.makeText(this, "Invalid user session. Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(userEmail, oldPassword);
        user.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Now update the password
                user.updatePassword(newPassword)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(ChangePassword.this, "Password updated successfully!", Toast.LENGTH_SHORT).show();
                            finish(); // Close activity after update
                        })
                        .addOnFailureListener(e -> Toast.makeText(ChangePassword.this, "Failed to update password: " + e.getMessage(), Toast.LENGTH_LONG).show());
            } else {
                oldPasswordLayout.setError("Incorrect current password");
                oldPasswordInput.requestFocus();
                Toast.makeText(ChangePassword.this, "Authentication failed. Please check your current password.", Toast.LENGTH_SHORT).show();
            }
        });


    }

    /**
     * Function to check password strength.
     * A strong password must be at least 8 characters long,
     * include at least one digit, one uppercase letter, and one special character.
     */
    private boolean isStrongPassword(String password) {
        return password.length() >= 6 &&
                password.matches(".*\\d.*") &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*[!@#$%^&*()_+=<>?/].*");
    }
}
