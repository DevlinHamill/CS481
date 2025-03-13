package edu.cwu.catmap.utilities;

import static edu.cwu.catmap.utilities.ToastHelper.showToast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;

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
    public void teststoreEvents(FirebaseUser user, HashMap<String,String> input, OnCompleteListener<DocumentReference> listener){
        db.collection(Constants.KEY_USER_COLLECTION).document(user.getUid()).collection("Events")
                .add(input)
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

    public void storeSchedule(FirebaseUser firebaseUser, Schedule schedule, OnCompleteListener<Void> listener) {
        db.collection(Constants.KEY_SCHEDULE_COLLECTION).document(firebaseUser.getUid()).set(schedule);
    }

    public void getSchedule(FirebaseUser firebaseUser, OnCompleteListener<DocumentSnapshot> listener) {
        db.collection(Constants.KEY_SCHEDULE_COLLECTION).document(firebaseUser.getUid()).get();
    }
}
