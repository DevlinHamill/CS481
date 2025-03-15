package edu.cwu.catmap.utilities;

import static edu.cwu.catmap.utilities.ToastHelper.showToast;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cwu.catmap.core.Event;
import edu.cwu.catmap.core.EventGroup;
import edu.cwu.catmap.core.Schedule;
import edu.cwu.catmap.manager.UserManager;

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

    /**
     * Add a location to the favorite locations table in the database.
     * @param locationName name of the location to add
     * @param color color of the favorite location
     * @param traceback traceback to return state
     */
    public void addFavoriteLocation(String locationName, int color, FirestoreTraceback traceback) {
        Log.i("Firestore Utility", "running addFavoriteLocation");
        HashMap <String, String> favoriteLocationMap = new HashMap<>();
        favoriteLocationMap.put(Constants.KEY_LOCATION_NAME, locationName);
        favoriteLocationMap.put(Constants.KEY_COLOR, String.valueOf(color));

        Log.i("Location info controller", "Hashmap created and populated, running database call");
        db.collection(Constants.KEY_USER_COLLECTION)
                .document(UserManager.getInstance().getCurrentFirebaseUser().getUid())
                .collection(Constants.KEY_FAVORITE_LOCATIONS_COLLECTION)
                .document(locationName) // Use locationName as the document ID
                .set(favoriteLocationMap, SetOptions.merge()) // Merge to avoid overwriting existing fields
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.i("Location info controller", "set with merge call was successful");
                        traceback.success("Location added to favorites");
                    } else {
                        Log.i("Location info controller", "set with merge call failed");
                        traceback.failure("Failed to add location to favorites");
                    }
                });
    }

    /**
     * Remove a location to the favorite locations table in the database.
     * @param locationName name of the location to add
     * @param traceback traceback to return state
     */
    public void removeFavoriteLocation(String locationName, FirestoreTraceback traceback) {
        //check to see if the location exists in the favorite locations table
        db.collection(Constants.KEY_USER_COLLECTION)
                .document(UserManager.getInstance().getCurrentFirebaseUser().getUid())
                .collection(Constants.KEY_FAVORITE_LOCATIONS_COLLECTION)
                .whereEqualTo(Constants.KEY_LOCATION_NAME, locationName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Location exists, get the document reference and delete it
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String documentId = queryDocumentSnapshots.getDocuments().get(0).getId();

                        db.collection(Constants.KEY_USER_COLLECTION)
                                .document(UserManager.getInstance().getCurrentFirebaseUser().getUid())
                                .collection(Constants.KEY_FAVORITE_LOCATIONS_COLLECTION)
                                .document(documentId)
                                .delete()
                                .addOnCompleteListener(task -> {
                                    traceback.success("Location removed from favorites");
                                });
                    }
                    //location does not exist, do nothing
                    else {
                        traceback.success("Location not found in favorites");
                    }
                }).addOnFailureListener(e -> {
                    //could not access the database
                    traceback.failure("Unable to access favorite locations table");
                });
    }

    public void getFavoriteLocations(FirestoreTraceback traceback) {
        db.collection(Constants.KEY_USER_COLLECTION)
                .document(UserManager.getInstance().getCurrentFirebaseUser().getUid())
                .collection(Constants.KEY_FAVORITE_LOCATIONS_COLLECTION)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        ArrayList<HashMap<String, String>> favoriteLocationsList = new ArrayList<>();
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            HashMap<String, String> favoriteLocation = convertDocumentToStringHashmap(document);

                            favoriteLocationsList.add(favoriteLocation);
                        }
                        traceback.success("Favorite locations retrieved", favoriteLocationsList);
                    } else {
                        traceback.success("No favorite locations found");
                    }
                })
                .addOnFailureListener(e -> {
                    traceback.failure("Unable to retrieve favorite locations");
                });
    }

    public void isFavoriteLocation(String locationName, FirestoreTraceback traceback) {
        //check to see if the location exists in the favorite locations table
        db.collection(Constants.KEY_USER_COLLECTION)
                .document(UserManager.getInstance().getCurrentFirebaseUser().getUid())
                .collection(Constants.KEY_FAVORITE_LOCATIONS_COLLECTION)
                .whereEqualTo(Constants.KEY_LOCATION_NAME, locationName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Location exists, get the document reference and delete it
                    if (!queryDocumentSnapshots.isEmpty()) {
                        traceback.success("Location in favorites!", true);
                    }
                    else {
                        traceback.success("Location not found in favorites", false);
                    }
                }).addOnFailureListener(e -> {
                    //could not access the database
                    traceback.failure("Unable to access favorite locations table");
                });
    }

    @NonNull
    private static HashMap<String, String> convertDocumentToStringHashmap(DocumentSnapshot document) {
        Map<String, Object> rawData = document.getData();
        HashMap<String, String> favoriteLocation = new HashMap<>();

        // Safely cast and ensure data integrity
        for (String key : rawData.keySet()) {
            Object value = rawData.get(key);
            if (value != null) {
                favoriteLocation.put(key, value.toString());
            } else {
                favoriteLocation.put(key, ""); // Default empty string if value is null
            }
        }
        return favoriteLocation;
    }

    public void storeSchedule(FirebaseUser firebaseUser, Schedule schedule, OnCompleteListener<Void> listener) {
        db.collection(Constants.KEY_SCHEDULE_COLLECTION).document(firebaseUser.getUid()).set(schedule);
    }

    public void getSchedule(FirebaseUser firebaseUser, OnCompleteListener<DocumentSnapshot> listener) {
        db.collection(Constants.KEY_SCHEDULE_COLLECTION).document(firebaseUser.getUid()).get();
    }
}
