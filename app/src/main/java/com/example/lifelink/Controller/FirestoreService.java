package com.example.lifelink.Controller;

import com.example.lifelink.Model.MedicalProfile;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class FirestoreService {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    // Fetch the medical profile from Firestore
    public void fetchMedicalProfile(final OnProfileFetchedListener listener) {
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("users").document(userId)
                .collection("medical_profile").document("profile")
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                MedicalProfile profile = document.toObject(MedicalProfile.class);
                                listener.onProfileFetched(profile);
                            } else {
                                listener.onError("Profile not found.");
                            }
                        } else {
                            listener.onError("Error fetching profile: " + task.getException());
                        }
                    }
                });
    }

    public interface OnProfileFetchedListener {
        void onProfileFetched(MedicalProfile profile);
        void onError(String error);
    }
}
