package edu.cwu.catmap.activities;

import android.content.Intent;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.base.MoreObjects;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

import edu.cwu.catmap.R;
import edu.cwu.catmap.databinding.ActivityLoginBinding;
import edu.cwu.catmap.manager.UserManager;
import edu.cwu.catmap.utilities.ToastHelper;

/**
 * Handles user login authentication, including validation, error handling,
 * and navigation to the main activity after successful login.
 */
public class Login extends AppCompatActivity {
    // sign in code that google uses
    private static final int RC_SIGN_IN = 9001;
    private ActivityLoginBinding binding;
    private UserManager userManager;
    private SignInClient signInClient;
    private FirebaseAuth mAuth;

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
        mAuth = FirebaseAuth.getInstance();
        setListeners();

        if(UserManager.getInstance().isLoggedIn()) {
            ToastHelper.showToast(this, "User session still valid");
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // success
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // failure
                showToast("Failed to authenticate with Google.");
            }
        }
    }

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

    private Bitmap scaleImage(Uri imageUri) {
        try {
            // Download the image
            InputStream inputStream = new URL(imageUri.toString()).openStream();

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
            inputStream = new URL(imageUri.toString()).openStream();
            Bitmap scaledBitmap = BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();

            return scaledBitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String encodeImage(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, byteArrayOutputStream); // Compression for storage efficiency
        return Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // do stuff after google login goes through
                        FirebaseUser user = mAuth.getCurrentUser();
                        // check if google account is new/first sign in (init user data)
                        if (user != null) {
                            if (task.getResult().getAdditionalUserInfo().isNewUser()) {
                                Uri imageUri = user.getPhotoUrl();
                                Log.d("Login", "imageUri: " + imageUri);
                                Bitmap scaledBitmap = scaleImage(imageUri);
                                String encodedProfilePicture = "";
                                if (scaledBitmap != null) {
                                    encodedProfilePicture = encodeImage(scaledBitmap);
                                } else {
                                    showToast("Failed to retrieve Google Accounts profile picture.");
                                }
                                userManager.signUpWithGoogle(user.getUid(), user.getEmail(), user.getDisplayName(), encodedProfilePicture,  // Save Base64 image
                                        signuptask -> {
                                            if (signuptask.isSuccessful()) {
                                                DocumentSnapshot userDocument = signuptask.getResult();
                                                if (userDocument != null && userDocument.exists()) {
                                                    showToast("Google sign in registration successful!");
                                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(intent);
                                                } else {
                                                    showToast("Google sign in registration failed");
                                                }
                                            } else {
                                                showToast("Google sign in registration failed");
                                            }
                                        });
                            }
                        }
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        showToast("Failed to authenticate with Google.");
                    }
                });
    }

    /**
     * Sets up click listeners for login, account creation, and password recovery actions.
     */
    private void setListeners() {

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        binding.googleSignin.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        );

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