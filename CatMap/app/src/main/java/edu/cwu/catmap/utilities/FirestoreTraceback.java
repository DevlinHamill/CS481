package edu.cwu.catmap.utilities;

/**
 * Traceback interface used to trigger events once database calls finish
 */
public interface FirestoreTraceback {

    /**
     * Database call successful with the following message
     * @param message message
     */
    void success(String message);

    /**
     * Database success with the following message and data
     * @param message message
     * @param data data (convert to desired class)
     */
    void success(String message, Object data);

    /**
     * Database failed to retrieve data with the following message
     * @param message message
     */
    void failure(String message);
}
