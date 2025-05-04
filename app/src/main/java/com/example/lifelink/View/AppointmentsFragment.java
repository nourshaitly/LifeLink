package com.example.lifelink.View;

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

import com.example.lifelink.Model.Appointment;
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

public class AppointmentsFragment extends Fragment implements AppointmentAdapter.OnAppointmentClickListener {

    private RecyclerView appointmentsRecycler;
    private FloatingActionButton addAppointmentFab;
    private List<Appointment> appointmentList;
    private AppointmentAdapter adapter;
    private ListenerRegistration appointmentListener;

    public AppointmentsFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_appointments, container, false);

        appointmentsRecycler = view.findViewById(R.id.appointmentsRecycler);
        addAppointmentFab = view.findViewById(R.id.addAppointmentFab);

        appointmentList = new ArrayList<>();
        adapter = new AppointmentAdapter(appointmentList, this);
        appointmentsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        appointmentsRecycler.setAdapter(adapter);

        addAppointmentFab.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), AddAppointmentActivity.class));
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        startAppointmentListener();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (appointmentListener != null) {
            appointmentListener.remove();
            appointmentListener = null;
        }
    }

    private void startAppointmentListener() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        CollectionReference appointmentsRef = db.collection("users")
                .document(userId)
                .collection("appointments");

        appointmentListener = appointmentsRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(getContext(), "Listen failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (snapshots != null) {
                    appointmentList.clear();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Appointment appointment = doc.toObject(Appointment.class);
                        if (appointment != null) {
                            appointment.setId(doc.getId()); // Set document ID
                            appointmentList.add(appointment);
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public void onMarkDoneClick(Appointment appointment) {
        // You can either delete or update a "done" field if you want
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() == null) return;
        String userId = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(userId)
                .collection("appointments")
                .document(appointment.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Appointment marked as done", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to mark as done: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onEditClick(Appointment appointment) {
        Intent intent = new Intent(getContext(), AddAppointmentActivity.class);
        intent.putExtra("appointment", appointment);
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(Appointment appointment) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Delete Appointment")
                .setMessage("Are you sure you want to delete this appointment?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    FirebaseAuth auth = FirebaseAuth.getInstance();

                    if (auth.getCurrentUser() == null) return;
                    String userId = auth.getCurrentUser().getUid();

                    db.collection("users")
                            .document(userId)
                            .collection("appointments")
                            .document(appointment.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Appointment deleted", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
