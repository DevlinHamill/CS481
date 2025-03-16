package edu.cwu.catmap.activities;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

import edu.cwu.catmap.databinding.ActivityForgotPasswordBinding;

public class ForgotPassword extends AppCompatActivity {

    private ActivityForgotPasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
    }


    private void setListeners() {
        binding.textSignIn.setOnClickListener(v -> onBackPressed());

        binding.resetPasswordButton.setOnClickListener(v -> sendEmail());
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void sendEmail() {
        if (binding.inputEmail.getText() != null){
            String email = binding.inputEmail.getText().toString().trim();

            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showToast("Please enter a valid e-mail.");
                return;
            }

            FirebaseAuth auth = FirebaseAuth.getInstance();
            auth.sendPasswordResetEmail(email)
                    .addOnSuccessListener(new OnSuccessListener() {
                        public void onSuccess(Object result) {
                            showToast("Password reset e-mail sent.");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        public void onFailure(Exception e) {
                            showToast("Failed to send password reset e-mail.");
                        }
                    });
        } else {
            showToast("Error retrieving input");
        }
    }
}
