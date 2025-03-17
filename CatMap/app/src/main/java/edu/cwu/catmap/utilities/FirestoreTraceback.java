package edu.cwu.catmap.utilities;

import android.util.Log;

/**
 * Traceback interface used to trigger events once database calls finish
 */
public interface FirestoreTraceback {

    /**
     * Database call successful with the following message
     * @param message message
     */
    default void success(String message) {
        Log.e("Firestore Traceback", "Un-overridden use of success method!");
    }

    /**
     * Database success with the following message and data
     * @param message message
     * @param data data (convert to desired class)
     */
    default void success(String message, Object data) {
        Log.e("Firestore Traceback", "Un-overridden use of success with data method!");
    }

    /**
     * Database failed to retrieve data with the following message
     * @param message message
     */
    default void failure(String message) {
        Log.e("Firestore Traceback", "Un-overridden use of failure method!");
    }
}
