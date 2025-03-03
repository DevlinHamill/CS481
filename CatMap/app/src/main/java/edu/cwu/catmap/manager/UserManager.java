package edu.cwu.catmap.manager;

import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import edu.cwu.catmap.core.User;
import edu.cwu.catmap.utilities.Constants;

/**
 * UserManager singleton class that will give access to some of the FirebaseAuth functionality.
 * Please keep in mind that all firebase functions that connect to the firebase database run
 * asynchronously so return must be handled asynchronously as well.
 */
public class UserManager {
    private static UserManager instance;
    private static User currentUser;
    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore db;
    private static final String defaultProfilePicture = ""; //this should be a base64 encoded default icon when the user doesn't choose one
    /**
     * Private constructor to generate the UserManager if it has not yet been instantiated
     */
    private UserManager() {
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Get instance method to get a reference to the instance of UserManager
     * @return the current instance of UserManager
     */
    public static synchronized UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }

        return instance;
    }

    /**
     * Create a new user account and create a document in the users table to hold their data
     * @param email email address to tie to the account
     * @param password password
     * @param name the users name
     * @param encodedProfilePicture base64 encoded profile picture. //TODO decide if and when encoding should be handled
     * @param listener listener to let the calling activity know when the operation is complete
     */
    public void signUp(String email, String password, String name, String encodedProfilePicture, OnCompleteListener<DocumentSnapshot> listener) {
        //create new user with FirebaseAuth
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                //if successful, continue to make new document in users collection
                .addOnSuccessListener(authResult -> {
                    if(authResult.getUser() != null) {
                        String userId = authResult.getUser().getUid();
                        Map<String, Object> userData = new HashMap<>();
                        userData.put(Constants.KEY_ENCODED_PROFILE_PICTURE, (TextUtils.isEmpty(encodedProfilePicture) ? defaultProfilePicture : encodedProfilePicture));
                        userData.put(Constants.KEY_NAME, name);
                        userData.put(Constants.KEY_PREF_LEFT_HANDED_MODE, false);
                        userData.put(Constants.KEY_PREF_WHEEL_CHAIR_MODE, false);
                        userData.put(Constants.KEY_PREF_DARK_MODE, false);
                        userData.put(Constants.KEY_PREF_COLOR_BLIND_MODE, Constants.VALUE_NORMAL_VISION);
                        userData.put(Constants.KEY_ENABLE_NOTIFICATIONS, true);
                        userData.put(Constants.KEY_MINUTES_BEFORE_EVENT_TO_NOTIFY, Constants.VALUE_DEFAULT_MINUTES_BEFORE_EVENT_TO_NOTIFY);

                        //add user data to the users collection
                        db.collection(Constants.KEY_USER_COLLECTION).document(userId).set(userData)
                                .addOnSuccessListener(setResult -> {
                                    //return feedback of completion to the calling activity
                                    currentUser = new User(name, email, encodedProfilePicture);
                                    db.collection(Constants.KEY_USER_COLLECTION).document(userId).get().addOnCompleteListener(listener);
                                })
                                //this will trigger if the user document cannot be created
                                .addOnFailureListener(e -> Log.e("SignUp", "Unable to create new user document in users collection", e));
                    }
                })
                //this will trigger if FirebaseAuth cannot make a new user
                .addOnFailureListener(e -> Log.e("SignUp", "Unable to create new user using FirebaseAuth", e));
    }

    /**
     * Attempt to log th user in with their provided email address and password.
     * @param email user email address
     * @param password user password
     * @param listener on complete listener used to return the state of the login result
     */
    public void signin(String email, String password, OnCompleteListener<DocumentSnapshot> listener) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    if(authResult.getUser() != null) {
                        db.collection(Constants.KEY_USER_COLLECTION).document(authResult.getUser().getUid()).get()
                                .addOnSuccessListener(userDocument -> {
                                    setCurrentUser(new User(Objects.requireNonNull(userDocument.getData())));
                                })
                                .addOnFailureListener(e -> Log.e("Login", "Unable to read user data from database", e))
                                .addOnCompleteListener(listener);
                    }
                })
                .addOnFailureListener(e -> Log.e("Login", "Unable to login user with FirebaseAuth", e));
    }

    /**
     * Log the current user out of the app.
     */
    public void logout() {
        firebaseAuth.signOut();
        setCurrentUser(null);
    }

    /**
     * Get the current user as a FirebaseUser object
     * @return user as FirebaseUser
     */
    public FirebaseUser getCurrentFirebaseUser() {
        return firebaseAuth.getCurrentUser();
    }

    /**
     * See if a user is currently logged in.
     * @return boolean yes/no
     */
    public boolean isLoggedIn() {
        return getCurrentFirebaseUser() != null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    private void setCurrentUser(User user) {
        currentUser = user;
    }

}
