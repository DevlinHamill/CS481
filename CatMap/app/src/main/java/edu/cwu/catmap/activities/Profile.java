package edu.cwu.catmap.activities;

import static edu.cwu.catmap.utilities.ToastHelper.showToast;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.makeramen.roundedimageview.RoundedImageView;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import edu.cwu.catmap.R;
import edu.cwu.catmap.utilities.Constants;

public class Profile extends AppCompatActivity {

    private RoundedImageView profileImage;
    private FloatingActionButton editProfileImageButton;
    private TextView profileName, profileEmail;
    private String encodedProfilePicture;
    private FirebaseAuth auth;
    private FirebaseFirestore database;
    private FirebaseUser currentUser;
    private static final String DEFAULT_PROFILE_PIC = ""; // Default Base64 image if needed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();

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
        LinearLayout changeUsernameButton = findViewById(R.id.changeUserName);
        LinearLayout changePasswordButton = findViewById(R.id.changePassword);
        TextView logOutButton = findViewById(R.id.logOut);


        editProfileImageButton.setOnClickListener(v -> openGallery());
        changeUsernameButton.setOnClickListener(v -> startActivity(new Intent(Profile.this, ChangeUserName.class)));
        changePasswordButton.setOnClickListener(v -> startActivity(new Intent(Profile.this, ChangePassword.class)));

        logOutButton.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(Profile.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
        });
    }


    /**
     * Checks if the user's profile exists in Firestore. If not, it creates one.
     */
    private void checkAndCreateUserProfile() {
        if (currentUser == null) return;

        database.collection(Constants.KEY_USER_COLLECTION)
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        createUserProfile();
                    } else {
                        loadUserProfile(documentSnapshot);
                    }
                })
                .addOnFailureListener(e -> Log.e("Profile", "Error checking user profile", e));
    }

    /**
     * Creates a new user document in Firestore and updates the UI.
     */
    private void createUserProfile() {
        if (currentUser == null) return;

        Map<String, Object> userData = new HashMap<>();
        userData.put(Constants.KEY_NAME, "Chakar Baloch");
        userData.put(Constants.KEY_ENCODED_PROFILE_PICTURE, DEFAULT_PROFILE_PIC);

        database.collection(Constants.KEY_USER_COLLECTION)
                .document(currentUser.getUid())
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    showToast(getApplicationContext(), "User profile created.");
                    profileName.setText("Chakar Baloch");
                    profileEmail.setText(currentUser.getEmail());

                    // Decode and set default profile picture
                    profileImage.setImageBitmap(decodeImage(DEFAULT_PROFILE_PIC));
                })
                .addOnFailureListener(e -> Log.e("Profile", "Failed to create user profile", e));
    }

    /**
     * Loads user data (name and profile picture) from Firestore and updates UI.
     */
    private void loadUserProfile(DocumentSnapshot documentSnapshot) {
        profileName.setText(documentSnapshot.getString(Constants.KEY_NAME));
        profileEmail.setText(currentUser.getEmail());

        String storedProfilePic = documentSnapshot.getString(Constants.KEY_ENCODED_PROFILE_PICTURE);
        if (storedProfilePic != null && !storedProfilePic.isEmpty()) {
            profileImage.setImageBitmap(decodeImage(storedProfilePic));
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

    /**
     * Handles image selection and uploads the new profile picture.
     */
    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(imageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        profileImage.setImageBitmap(bitmap);

                        // Corrected: Store encoded string in Firestore
                        encodedProfilePicture = encodeImage(bitmap);
                        saveProfilePictureToFirestore();

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
    );

    /**
     * Saves the updated profile picture to Firestore.
     */
    private void saveProfilePictureToFirestore() {
        if (currentUser == null || encodedProfilePicture == null) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_ENCODED_PROFILE_PICTURE, encodedProfilePicture);

        database.collection(Constants.KEY_USER_COLLECTION)
                .document(currentUser.getUid())
                .update(updates)
                .addOnSuccessListener(aVoid -> showToast(getApplicationContext(), "Profile picture updated successfully"))
                .addOnFailureListener(e -> Log.e("Profile", "Failed to update profile picture", e));
    }

    /**
     * Encodes a Bitmap image into a Base64 string.
     */
    private String encodeImage(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        return Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
    }

    /**
     * Decodes a Base64-encoded string back into a Bitmap.
     */
    private Bitmap decodeImage(String encodedString) {
        byte[] bytes = Base64.decode(encodedString, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
