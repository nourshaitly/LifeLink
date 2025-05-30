package com.example.lifelink.Controller;

import com.example.lifelink.Model.MedicalProfile;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

/**
 * FirestoreService is a helper class responsible for interacting with
 * Firebase Firestore to retrieve medical profile data for the currently
 * authenticated user.
 */
public class FirestoreService {

    // Instance of Firestore database
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Firebase Authentication instance to get current user UID
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    /**
     * Fetches the current user's medical profile from Firestore.
     *
     * The profile is expected to be located at:
     * users/{userId}/medical_profile/profile
     *
     * This method is asynchronous and the result is delivered via the
     * OnProfileFetchedListener interface.
     *
     * @param listener An implementation of OnProfileFetchedListener to handle success or error
     */
    public void fetchMedicalProfile(final OnProfileFetchedListener listener) {
        // Get the UID of the currently logged-in user
        String userId = mAuth.getCurrentUser().getUid();

        // Navigate Firestore to: users/{uid}/medical_profile/profile
        db.collection("users").document(userId)
                .collection("medical_profile").document("profile")
                .get() // Asynchronously get the document
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(Task<DocumentSnapshot> task) {
                        // If Firestore request was successful
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();

                            if (document.exists()) {
                                // Convert the Firestore document into a MedicalProfile object
                                MedicalProfile profile = document.toObject(MedicalProfile.class);

                                // Callback with the profile to the caller
                                listener.onProfileFetched(profile);
                            } else {
                                // Document not found — notify the caller
                                listener.onError("Profile not found.");
                            }
                        } else {
                            // Some other error occurred — notify the caller with exception details
                            listener.onError("Error fetching profile: " + task.getException());
                        }
                    }
                });
    }

    /**
     * Interface definition for a callback to be invoked when the medical
     * profile is fetched from Firestore or an error occurs.
     */
    public interface OnProfileFetchedListener {
        /**
         * Called when the medical profile is successfully retrieved.
         *
         * @param profile The retrieved MedicalProfile object
         */
        void onProfileFetched(MedicalProfile profile);

        /**
         * Called when an error occurs during profile retrieval.
         *
         * @param error A message describing the error
         */
        void onError(String error);
    }
}
