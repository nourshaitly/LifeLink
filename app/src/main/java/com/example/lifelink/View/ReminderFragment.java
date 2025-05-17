package com.example.lifelink.View;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lifelink.Model.Reminder;
import com.example.lifelink.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ReminderFragment extends Fragment implements ReminderAdapter.OnReminderClickListener {

    private RecyclerView reminderListRecycler;
    private FloatingActionButton addReminderFab;
    private List<Reminder> reminderList;
    private ReminderAdapter reminderAdapter;
    private ListenerRegistration reminderListener;


    public ReminderFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_reminder_fragment, container, false);

        reminderListRecycler = view.findViewById(R.id.reminderListRecycler);
        addReminderFab = view.findViewById(R.id.addReminderFab);

        reminderList = new ArrayList<>();
        reminderAdapter = new ReminderAdapter(reminderList, this);
        reminderListRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        reminderListRecycler.setAdapter(reminderAdapter);

        addReminderFab.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), AddReminderActivity.class));
        });

        return view;


    }

    @Override
    public void onStart() {
        super.onStart();
        startReminderListener();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (reminderListener != null) {
            reminderListener.remove();
            reminderListener = null;
        }
    }

    private void startReminderListener() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        CollectionReference remindersRef = db.collection("users")
                .document(userId)
                .collection("reminders");

        reminderListener = remindersRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(getContext(), "Listen failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (snapshots != null) {
                    reminderList.clear();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Reminder reminder = doc.toObject(Reminder.class);
                        if (reminder != null) {
                            reminder.setId(doc.getId()); // Ensure the ID is set
                            reminderList.add(reminder);
                        }
                    }
                    reminderAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public void onEditClick(Reminder reminder) {
        Intent intent = new Intent(getContext(), AddReminderActivity.class);
        intent.putExtra("reminder", reminder);
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(Reminder reminder) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Delete Reminder")
                .setMessage("Are you sure you want to delete this reminder?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    FirebaseAuth auth = FirebaseAuth.getInstance();

                    if (auth.getCurrentUser() == null) {
                        Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String userId = auth.getCurrentUser().getUid();

                    db.collection("users")
                            .document(userId)
                            .collection("reminders")
                            .document(reminder.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Reminder deleted", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override

    public void onMarkAsTakenClick(Reminder reminder) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Mark as Taken")
                .setMessage("Are you sure you want to mark this reminder as taken?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    markReminderAsTaken(reminder); // ✅ If user clicks Yes, mark as taken
                    cancelReminderAlarm(reminder); // ✅ After marking as taken, cancel alarm
                })
                .setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss(); // ❌ If user clicks No, dismiss the dialog
                })
                .show();
    }
    private void markReminderAsTaken(Reminder reminder) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(userId)
                .collection("reminders")
                .document(reminder.getId())
                .update("taken", true)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Marked as taken ✅", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to mark as taken: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void cancelReminderAlarm(Reminder reminder) {
        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(requireContext(), ReminderAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                requireContext(),
                reminder.getId().hashCode(), // use same ID you used when setting alarm
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent); // ✅ Cancel the scheduled alarm
        }
    }

}
