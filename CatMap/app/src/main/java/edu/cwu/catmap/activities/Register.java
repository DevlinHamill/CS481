package edu.cwu.catmap.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import edu.cwu.catmap.databinding.ActivityRegisterBinding;
import edu.cwu.catmap.manager.UserManager;
import edu.cwu.catmap.utilities.Constants;

/**
 * Handles user registration, including form validation, Firebase authentication,
 * profile image selection, and user data storage.
 */
public class Register extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private UserManager userManager;
    private String encodedProfilePicture;

    /**
     * Initializes the Register activity, setting up UI components and user registration logic.
     *
     * @param savedInstanceState Previous saved state, if available.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        userManager = UserManager.getInstance();
        setListeners();
    }

    /**
     * Sets up click listeners for sign-in, back navigation, and user registration.
     */
    private void setListeners() {
        binding.logOut.setOnClickListener(v -> onBackPressed());
        binding.textSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(Register.this, Login.class);
            startActivity(intent);
        });

        binding.registerButton.setOnClickListener(v -> {
            if (isValidateSignUpDetails()) {
                userManager.signUp(
                        binding.inputEmail.getText().toString(),
                        binding.inputPassword.getText().toString(),
                        binding.inputName.getText().toString(),
                        encodedProfilePicture,  // Save Base64 image
                        task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot userDocument = task.getResult();
                                if (userDocument != null && userDocument.exists()) {
                                    showToast("Registration successful!");
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                } else {
                                    showToast("Registration failed");
                                }
                            } else {
                                showToast("Registration failed");
                            }
                        });
            }
        });

        // Open gallery for profile image selection
        binding.imageProfile.setOnClickListener(v -> openGallery());
    }

    /**
     * Displays a short toast message.
     *
     * @param message The message to display.
     */
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Opens the gallery for selecting an image.
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        pickImage.launch(intent);
    }

    /**
     * Handles the result of image selection.
     */
    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();

                    Bitmap scaledBitmap = scaleImage(imageUri);
                    if (scaledBitmap != null) {
                        binding.imageProfile.setImageBitmap(scaledBitmap);
                        encodedProfilePicture = encodeImage(scaledBitmap);
                    } else {
                        showToast("Failed to load image.");
                    }
                }
            }
    );

    /**
     * Encodes a Bitmap image into a Base64 string with optimized quality.
     */
    private String encodeImage(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, byteArrayOutputStream); // Compression for storage efficiency
        return Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
    }

    /**
     * Decodes a Base64-encoded string back into a Bitmap.
     */
    private Bitmap decodeImage(String encodedString) {
        byte[] bytes = Base64.decode(encodedString, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    /**
     * Scales an image efficiently while maintaining clarity.
     */
    private Bitmap scaleImage(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);

            // Decode the image size without loading the full bitmap into memory
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();

            // Calculate optimal inSampleSize
            int targetWidth = 150; // Target size
            int targetHeight = 150;
            options.inSampleSize = calculateInSampleSize(options, targetWidth, targetHeight);

            // Decode with inSampleSize to get a scaled-down version
            options.inJustDecodeBounds = false;
            inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap scaledBitmap = BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();

            return scaledBitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Calculates the best inSampleSize for efficient downscaling.
     */
    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    /**
     * Validates user input before registration with inline error messages.
     */
    private Boolean isValidateSignUpDetails() {
        String name = binding.inputName.getText().toString().trim();
        String email = binding.inputEmail.getText().toString().trim();
        String password = binding.inputPassword.getText().toString();
        String confirmPassword = binding.inputConFirmPassword.getText().toString();

        // Validate Profile Picture Selection
//        if (encodedProfilePicture == null) {
//            showToast("Please choose an avatar");
//            return false;
//        }

        // Validate Name
        if (TextUtils.isEmpty(name)) {
            binding.inputNameLayout.setError("Please enter your name");
            binding.inputName.requestFocus();
            return false;
        } else {
            binding.inputNameLayout.setError(null);
        }

        // Validate Email
        if (TextUtils.isEmpty(email)) {
            binding.inputEmailLayout.setError("Please enter your e-mail");
            binding.inputEmail.requestFocus();
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.inputEmailLayout.setError("Please enter a valid e-mail");
            binding.inputEmail.requestFocus();
            return false;
        } else {
            binding.inputEmailLayout.setError(null);
        }

        // Validate Password Strength
        if (TextUtils.isEmpty(password)) {
            binding.inputPasswordLayout.setError("Please enter your password");
            binding.inputPassword.requestFocus();
            return false;
        } else if (!isStrongPassword(password)) {
            binding.inputPasswordLayout.setError("Must be 8+ letters, include a number, uppercase & special character.");
            binding.inputPassword.requestFocus();
            return false;
        } else {
            binding.inputPasswordLayout.setError(null);
        }

        // Validate Confirm Password
        if (TextUtils.isEmpty(confirmPassword)) {
            binding.inputConFirmPasswordLayout.setError("Please confirm your password");
            binding.inputConFirmPassword.requestFocus();
            return false;
        } else if (!password.equals(confirmPassword)) {
            binding.inputConFirmPasswordLayout.setError("Passwords do not match");
            binding.inputConFirmPassword.requestFocus();
            return false;
        } else {
            binding.inputConFirmPasswordLayout.setError(null);
        }

        return true;
    }

    private boolean isStrongPassword(String password) {
        return password.length() >= 6 &&
                password.matches(".*\\d.*") &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*[!@#$%^&*()_+=<>?/].*");
    }
}
