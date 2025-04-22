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

import java.util.ArrayList;
import java.util.List;

public class AppointmentsFragment extends Fragment {

    private RecyclerView appointmentsRecycler;
    private FloatingActionButton addAppointmentFab;
    private List<Appointment> appointmentList;
    private AppointmentAdapter adapter;

    public AppointmentsFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_appointments, container, false);

        appointmentsRecycler = view.findViewById(R.id.appointmentsRecycler);
        addAppointmentFab = view.findViewById(R.id.addAppointmentFab);

        // Test data
        appointmentList = new ArrayList<>();
        appointmentList.add(new Appointment("Dr. Firas Ayoub", "Apr 22, 2025", "3:00 PM", "Clemenceau Hospital"));
        appointmentList.add(new Appointment("Dr. Nada Saad", "Apr 26, 2025", "10:30 AM", "Beirut Medical Center"));

        adapter = new AppointmentAdapter(appointmentList);
        appointmentsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        appointmentsRecycler.setAdapter(adapter);

        addAppointmentFab.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "FAB clicked", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(requireContext(), AddAppointmentActivity.class));
           // Toast.makeText(requireContext(), "Trying to launch: " + AddAppointmentActivity.class.getName(), Toast.LENGTH_SHORT).show();

        });

        return view;
    }
}
