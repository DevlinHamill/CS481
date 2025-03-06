package edu.cwu.catmap.utilities;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cwu.catmap.core.Event;
import edu.cwu.catmap.core.EventGroup;
import edu.cwu.catmap.core.Schedule;
import edu.cwu.catmap.managers.UserManager;

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

    /**
     * Add a location to the favorite locations table in the database.
     * @param locationName name of the location to add
     * @param color color of the favorite location
     * @param traceback traceback to return state
     */
    public void addFavoriteLocation(String locationName, int color, FirestoreTraceback traceback) {
        //check to see if the favorite location is already in the database
        db.collection(Constants.KEY_USER_COLLECTION)
                .document(UserManager.getInstance().getCurrentFirebaseUser().getUid())
                .collection(Constants.KEY_FAVORITE_LOCATIONS_COLLECTION)
                .whereEqualTo(Constants.KEY_LOCATION_NAME, locationName)
                .get()
                .addOnSuccessListener( queryDocumentSnapshots -> {
                    //if the returned document is empty, that means it does not already exist, so add it
                    if(queryDocumentSnapshots.isEmpty()) {
                        //make hashmap to store data
                        HashMap<String, String> favoriteLocationMap = new HashMap<>();
                        favoriteLocationMap.put(Constants.KEY_LOCATION_NAME, locationName);
                        favoriteLocationMap.put(Constants.KEY_COLOR, String.valueOf(color));

                        //store the favorite location data in the database
                        db.collection(Constants.KEY_USER_COLLECTION)
                                .document(UserManager.getInstance().getCurrentFirebaseUser().getUid())
                                .collection(Constants.KEY_FAVORITE_LOCATIONS_COLLECTION)
                                .add(favoriteLocationMap)
                                .addOnCompleteListener(task -> {
                                    traceback.success("Location added to favorites");
                                });
                    }
                    else {
                        //if it does exist, do nothing and return the reference
                        traceback.success("Location already in favorites");

                    }
                }).addOnFailureListener(e -> {
                    //failed to retrieve the document where name = name
                    traceback.failure("Unable to access favorite locations table");
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
                        List<HashMap<String, String>> favoriteLocationsList = new ArrayList<>();
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

    public void storeSchedule(FirebaseUser firebaseUser, Schedule schedule, OnCompleteListener<Void> listener) {
        db.collection(Constants.KEY_SCHEDULE_COLLECTION).document(firebaseUser.getUid()).set(schedule);
    }

    public void getSchedule(FirebaseUser firebaseUser, OnCompleteListener<DocumentSnapshot> listener) {
        db.collection(Constants.KEY_SCHEDULE_COLLECTION).document(firebaseUser.getUid()).get();
    }
}
