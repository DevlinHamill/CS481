package edu.cwu.catmap.activities;

import static android.content.ContentValues.TAG;
import static edu.cwu.catmap.utilities.ToastHelper.showToast;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.makeramen.roundedimageview.RoundedImageView;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Objects;

import edu.cwu.catmap.R;
import edu.cwu.catmap.manager.UserManager;
import edu.cwu.catmap.utilities.Constants;

/**
 * Handles user profile functionality, including displaying profile details,
 * updating profile pictures, and logging out.
 */
public class Profile extends AppCompatActivity {

    private RoundedImageView profileImage;
    private FloatingActionButton editProfileImageButton;
    private TextView profileName, profileEmail, profileHeader;
    private String encodedProfilePicture;
    private FirebaseAuth auth;
    private FirebaseFirestore database;
    private UserManager userManager;
    private ListenerRegistration profileListener;


    /**
     * Initializes the Profile activity, setting up UI components
     * and retrieving user profile data from Firestore.
     *
     * @param savedInstanceState Previous saved state, if available.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
        userManager = UserManager.getInstance();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profileScrollView), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI elements
        profileImage = findViewById(R.id.profileImage);
        editProfileImageButton = findViewById(R.id.editProfileImage);
        profileName = findViewById(R.id.profileName);
        profileEmail = findViewById(R.id.profileEmail);
        profileHeader = findViewById(R.id.profileHeader);
        LinearLayout changeUsernameButton = findViewById(R.id.changeUserName);
        LinearLayout changePasswordButton = findViewById(R.id.changePassword);
        TextView logOutButton = findViewById(R.id.logOut);

        // Load user details when profile page opens
        listenForProfileUpdates();
        FirebaseUser currentUser = auth.getCurrentUser();
        String userId = null;
        if (currentUser != null) {
            userId = currentUser.getUid();
            DocumentReference userDocRef = database.collection(Constants.KEY_USER_COLLECTION).document(userId);
            userDocRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists() && document.contains(Constants.KEY_ACCOUNT_TYPE)) {
                        if (Objects.equals(document.getString(Constants.KEY_ACCOUNT_TYPE), Constants.VALUE_ACCOUNT_GOOGLE)) {
                            editProfileImageButton.setVisibility(View.GONE);
                            changeUsernameButton.setVisibility(View.GONE);
                            changePasswordButton.setVisibility(View.GONE);
                            profileHeader.setText("Google Account");
                            Log.d(TAG, "User account type: " + document.getString("account_type"));
                        } else {
                            editProfileImageButton.setOnClickListener(v -> openGallery());
                            changeUsernameButton.setOnClickListener(v -> startActivity(new Intent(Profile.this, ChangeUserName.class)));
                            changePasswordButton.setOnClickListener(v -> startActivity(new Intent(Profile.this, ChangePassword.class)));
                            Log.d(TAG, "User account type: " + document.getString("account_type"));
                        }
                    } else {
                        editProfileImageButton.setVisibility(View.GONE);
                        changeUsernameButton.setVisibility(View.GONE);
                        changePasswordButton.setVisibility(View.GONE);
                        profileHeader.setText("Account Type Error");
                        Log.d(TAG, "User data file not found");
                    }
                } else {
                    editProfileImageButton.setVisibility(View.GONE);
                    changeUsernameButton.setVisibility(View.GONE);
                    changePasswordButton.setVisibility(View.GONE);
                    profileHeader.setText("Account Database Connection Error");
                    Log.d(TAG, "Firestore GET failed", task.getException());
                }
            });
        } else {
            editProfileImageButton.setVisibility(View.GONE);
            changeUsernameButton.setVisibility(View.GONE);
            changePasswordButton.setVisibility(View.GONE);
            profileHeader.setText("Account Database Connection Error");
            Log.e(TAG, "Failed to retrieve current user.");
        }



        logOutButton.setOnClickListener(v -> {
            auth.signOut();
            userManager.logout();
            Intent intent = new Intent("STOP_REFRESHTASK");
            sendBroadcast(intent);
            startActivity(new Intent(Profile.this, Login.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
        });
    }

    /**
     * Listens for real-time updates to the user's profile.
     */
    private void listenForProfileUpdates() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DocumentReference userDocRef = database.collection(Constants.KEY_USER_COLLECTION).document(userId);

            profileListener = userDocRef.addSnapshotListener((documentSnapshot, error) -> {
                if (error != null) {
                    Log.e("Profile", "Error listening to profile updates", error);
                    return;
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    String name = documentSnapshot.getString(Constants.KEY_NAME);
                    String email = currentUser.getEmail();
                    encodedProfilePicture = documentSnapshot.getString(Constants.KEY_ENCODED_PROFILE_PICTURE);

                    profileName.setText(name);
                    profileEmail.setText(email);

                    // Decode and set the profile image if available
                    if (encodedProfilePicture != null && !encodedProfilePicture.isEmpty()) {
                        profileImage.setImageBitmap(decodeImage(encodedProfilePicture));
                    }
                }
            });
        }
    }

    /**
     * Opens the gallery for selecting an image.
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        pickImage.launch(intent);
    }


    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();

                    Bitmap scaledBitmap = scaleImage(imageUri);
                    if (scaledBitmap != null) {
                        profileImage.setImageBitmap(scaledBitmap);

                        // Encode and update Firestore
                        encodedProfilePicture = encodeImage(scaledBitmap);
                        updateProfilePicture(encodedProfilePicture);
                    } else {
                        showToast(this, "Failed to load image.");
                    }
                }
            }
    );

    /**
     * Updates the user's profile picture in Firestore.
     *
     * @param encodedImage Base64-encoded image string.
     */
    private void updateProfilePicture(String encodedImage) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            database.collection(Constants.KEY_USER_COLLECTION).document(userId)
                    .update(Constants.KEY_ENCODED_PROFILE_PICTURE, encodedImage)
                    .addOnSuccessListener(aVoid -> showToast(this, "Profile picture updated successfully!"))
                    .addOnFailureListener(e -> Log.e("Profile", "Failed to update profile picture", e));
        }
    }

    /**
     * Encodes a Bitmap image into a Base64 string.
     *
     * @param bitmap The image to be encoded.
     * @return Base64 string of the image.
     */
    private String encodeImage(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, byteArrayOutputStream); // Higher quality
        return Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
    }



    /**
     * Decodes a Base64-encoded image string back into a Bitmap.
     *
     * @param encodedString The Base64 string representing the image.
     * @return Decoded Bitmap image.
     */
    private Bitmap decodeImage(String encodedString) {
        byte[] bytes = Base64.decode(encodedString, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    /**
     * Scales an image to fit within specified dimensions while maintaining quality.
     *
     * @param imageUri The URI of the image to be scaled.
     * @return The scaled Bitmap image.
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
     * Calculates the best sample size for efficient image downscaling.
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


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (profileListener != null) {
            profileListener.remove();
        }
    }
}
