package edu.cwu.catmap.utilities;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import edu.cwu.catmap.core.Event;
import edu.cwu.catmap.core.EventGroup;
import edu.cwu.catmap.core.Schedule;

/**
 * FirestoreUtility class used to access the firebase firestore database to get schedule schedule
 * and favorite locations information. user info database access is managed through the UserManager class
 */
public class FirestoreUtility {
    private static FirestoreUtility instance;
    private final FirebaseFirestore db;

    private FirestoreUtility() {
        db = FirebaseFirestore.getInstance();
    }

    public static FirestoreUtility getInstance() {
        if(instance == null) {
            instance = new FirestoreUtility();
        }

        return instance;
    }

    public void getEvents(FirebaseUser firebaseUser, OnCompleteListener<DocumentSnapshot> listener) {
        db.collection(Constants.KEY_EVENT_COLLECTION).document(firebaseUser.getUid()).get()
                .addOnCompleteListener(listener);
    }

    public void storeEvents(FirebaseUser firebaseUser, ArrayList<Event> events, OnCompleteListener<Void> listener) {
        db.collection(Constants.KEY_EVENT_COLLECTION).document(firebaseUser.getUid()).set(events)
                .addOnCompleteListener(listener);
    }

    public void getEventGroups(FirebaseUser firebaseUser, OnCompleteListener<DocumentSnapshot> listener) {
        db.collection(Constants.KEY_EVENT_GROUP_COLLECTION).document(firebaseUser.getUid()).get()
                .addOnCompleteListener(listener);
    }

    public void storeEventGroups(FirebaseUser firebaseUser, ArrayList<EventGroup> eventGroups, OnCompleteListener<Void> listener) {
        db.collection(Constants.KEY_EVENT_GROUP_COLLECTION).document(firebaseUser.getUid()).set(eventGroups)
                .addOnCompleteListener(listener);
    }

    public void getFavoriteLocations(FirebaseUser firebaseUser, OnCompleteListener<DocumentSnapshot> listener) {
        db.collection(Constants.KEY_FAVORITE_LOCATION_COLLECTION).document(firebaseUser.getUid()).get()
                .addOnCompleteListener(listener);
    }

    public void storeFavoriteLocations(FirebaseUser firebaseUser, ArrayList<EventGroup> eventGroups, OnCompleteListener<Void> listener) {
        db.collection(Constants.KEY_FAVORITE_LOCATION_COLLECTION).document(firebaseUser.getUid()).set(eventGroups)
                .addOnCompleteListener(listener);
    }
}
