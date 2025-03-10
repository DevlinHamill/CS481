package edu.cwu.catmap.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import edu.cwu.catmap.R;
import edu.cwu.catmap.utilities.Constants;

public class ChangeUserName extends AppCompatActivity {

    private TextInputEditText usernameInput;
    private TextInputLayout usernameInputLayout;
    private MaterialButton updateUsernameButton;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_user_name);

        // Apply insets for fullscreen mode
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.usernameScrollView), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
        currentUser = auth.getCurrentUser();

        // Bind UI elements
        usernameInput = findViewById(R.id.newUsernameInput);
        usernameInputLayout = findViewById(R.id.newUsernameInputLayout);
        updateUsernameButton = findViewById(R.id.updateUsernameButton);

        // Set button click listener
        updateUsernameButton.setOnClickListener(v -> updateUsername());
    }

    /**
     * Updates the username in Firebase Firestore.
     */
    private void updateUsername() {
        if (currentUser == null) {
            Toast.makeText(this, "User not signed in!", Toast.LENGTH_SHORT).show();
            return;
        }

        String newUsername = usernameInput.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(newUsername)) {
            usernameInputLayout.setError("Username cannot be empty");
            usernameInput.requestFocus();
            return;
        } else {
            usernameInputLayout.setError(null);
        }

        // Update username in Firestore
        database.collection(Constants.KEY_USER_COLLECTION)
                .document(currentUser.getUid())
                .update(Constants.KEY_NAME, newUsername)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ChangeUserName.this, "Username updated successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Close the activity after update
                })
                .addOnFailureListener(e -> {
                    Log.e("ChangeUserName", "Failed to update username", e);
                    Toast.makeText(ChangeUserName.this, "Failed to update username", Toast.LENGTH_SHORT).show();
                });
    }
}
