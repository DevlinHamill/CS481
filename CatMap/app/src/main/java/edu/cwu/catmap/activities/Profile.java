package edu.cwu.catmap.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

import edu.cwu.catmap.R;

public class Profile extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Find buttons by their IDs
        MaterialButton changeUsernameButton = findViewById(R.id.changeUserName);
        MaterialButton changePasswordButton = findViewById(R.id.changePassword);
        MaterialButton recoverAccountButton = findViewById(R.id.recoverAccount);
        MaterialButton logOutButton = findViewById(R.id.logOut);

        // Set onClickListeners for each button
        changeUsernameButton.setOnClickListener(v -> {
            Intent intent = new Intent(Profile.this, ChangeUserName.class);
            startActivity(intent);
        });

        changePasswordButton.setOnClickListener(v -> {
            Intent intent = new Intent(Profile.this, ChangePassword.class);
            startActivity(intent);
        });

        recoverAccountButton.setOnClickListener(v -> {
            Intent intent = new Intent(Profile.this, RecoveryInfo.class);
            startActivity(intent);
        });

//        logOutButton.setOnClickListener(v -> {
//            // Handle logout (Clear session, go back to Login screen)
//            Intent intent = new Intent(Profile.this, Login.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clears activity stack
//            startActivity(intent);
//            finish();
//        });
    }



}