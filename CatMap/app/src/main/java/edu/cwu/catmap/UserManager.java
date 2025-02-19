package edu.cwu.catmap;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;

import java.util.function.Consumer;

/**
 * UserManager singleton class that will give access to some of the FirebaseAuth functionality.
 * Please keep in mind that all firebase functions that connect to the firebase database run
 * asynchronously so return must be handled asynchronously as well.
 */
public class UserManager {

    private static UserManager instance;
    private final FirebaseAuth firebaseAuth;

    /**
     * Private constructor to generate the UserManager if it has not yet been instantiated
     */
    private UserManager() {
        firebaseAuth = FirebaseAuth.getInstance();
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
     * Sign a user up to the app using FirebaseAuth
     * @param email email address of the user
     * @param password desired password of the user
     * @param listener on complete listener used to return the state of the login result
     */
    public void signUp(String email, String password, OnCompleteListener<AuthResult> listener) {
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(listener);
    }

    /**
     * Attempt to log th user in with their provided email address and password.
     * @param email user email address
     * @param password user password
     * @param listener on complete listener used to return the state of the login result
     */
    public void login(String email, String password, OnCompleteListener<AuthResult> listener) {
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(listener);
    }

    /**
     * Log the current user out of the app.
     */
    public void logout() {
        firebaseAuth.signOut();
    }

    /**
     * Get the current user as a FirebaseUser object
     * @return user as FirebaseUser
     */
    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    /**
     * See if a user is currently logged in.
     * @return boolean yes/no
     */
    public boolean isLoggedIn() {
        return getCurrentUser() != null;
    }

}
