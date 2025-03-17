package edu.cwu.catmap.utilities;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cwu.catmap.core.ClassItem;
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
    public void teststoreEvents(FirebaseUser user, HashMap<String,String> input, OnCompleteListener<DocumentReference> listener){
        db.collection(Constants.KEY_USER_COLLECTION).document(user.getUid()).collection("Events")
                .add(input)
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
                .collection(Constants.KEY_FAVORITE_LOCATIONS_SUBCOLLECTION)
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
                .collection(Constants.KEY_FAVORITE_LOCATIONS_SUBCOLLECTION)
                .whereEqualTo(Constants.KEY_LOCATION_NAME, locationName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Location exists, get the document reference and delete it
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String documentId = queryDocumentSnapshots.getDocuments().get(0).getId();

                        db.collection(Constants.KEY_USER_COLLECTION)
                                .document(UserManager.getInstance().getCurrentFirebaseUser().getUid())
                                .collection(Constants.KEY_FAVORITE_LOCATIONS_SUBCOLLECTION)
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
                .collection(Constants.KEY_FAVORITE_LOCATIONS_SUBCOLLECTION)
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
                .collection(Constants.KEY_FAVORITE_LOCATIONS_SUBCOLLECTION)
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

    public void getClasses(FirestoreTraceback traceback) {
        Log.i("Firestore Utility", "fetching classes from db");
        String currentUID = UserManager.getInstance().getCurrentFirebaseUser().getUid();

        Log.i("Firestore Utility", "current user ID: " + currentUID);

        db.collection(Constants.KEY_USER_COLLECTION)
                .document(currentUID)
                .collection(Constants.KEY_EVENTS_SUBCOLLECTION)
                .whereEqualTo(Constants.KEY_EVENT_TYPE, Constants.VALUE_EVENT_TYPE_CLASS)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    //set to collect distinct class names so no duplicates
                    Set<ClassItem> classItemSet = new HashSet<>();

                    Log.i("Firestore Utility", "queryDocumentSnapshots size: " + queryDocumentSnapshots.size());

                    //add class names to set
                    for(DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String title = (String) documentSnapshot.get(Constants.KEY_EVENT_TITLE);
                        int color = Integer.parseInt((String) documentSnapshot.get(Constants.KEY_EVENT_COLOR));
                        if(title != null) {
                            ClassItem classItem = new ClassItem(title, color);
                            Log.i("Firestore Utility", "new classItem added to set: " + classItem);
                            classItemSet.add(classItem);
                        }
                    }

                    ArrayList<ClassItem> classItemsList = new ArrayList<>(classItemSet);

                    traceback.success("Class names retrieved successfully!", classItemsList);
                })
                .addOnFailureListener(e -> {
                    traceback.failure("Could not connect to the database!");
                });
    }

    public void removeClass(String className, FirestoreTraceback traceback) {
        Log.i("Firestore Utility", "removing class: " + className + " from the database");
        String currentUID = UserManager.getInstance().getCurrentFirebaseUser().getUid();

        Log.i("Firestore Utility", "current user ID: " + currentUID);

        db.collection(Constants.KEY_USER_COLLECTION)
                .document(currentUID)
                .collection(Constants.KEY_EVENTS_SUBCOLLECTION)
                .whereEqualTo(Constants.KEY_EVENT_TITLE, className)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if(queryDocumentSnapshots.isEmpty()) {
                        Log.i("Firestore Utility", "No events found for class: " + className);
                        traceback.failure("No events found for class: " + className);
                        return;
                    }

                    List<Task<Void>> deletionTasks = new ArrayList<>();

                    for(DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        Log.i("Firestore Utility", "Deleting event of class: " + documentSnapshot.getString(Constants.KEY_EVENT_TITLE));
                        deletionTasks.add(documentSnapshot.getReference().delete()
                                .addOnFailureListener(e -> Log.e("Firestore Utility", "Failed to delete event: " + documentSnapshot.getId(), e)));
                    }

                    Tasks.whenAllSuccess(deletionTasks)
                            .addOnSuccessListener(command -> {
                                Log.i("Firestore Utility", "Successfully deleted all events for class: " + className);
                                traceback.success("Deleted all events of class " + className);
                            })
                            .addOnFailureListener(e -> {
                                Log.e("Firestore Utility", "Error deleting some events", e);
                                traceback.failure("Error occurred while deleting events.");
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore Utility", "Firestore query failed", e);
                    traceback.failure("Unable to connect to the database!");
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
}
