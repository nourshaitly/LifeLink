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

import com.example.lifelink.Model.Reminder;
import com.example.lifelink.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ReminderFragment extends Fragment {

    private RecyclerView reminderListRecycler;
    private FloatingActionButton addReminderFab;

    private List<Reminder> reminderList;
    private ReminderAdapter reminderAdapter;

    public ReminderFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_reminder_fragment, container, false);

        // Initialize views from the inflated layout
        reminderListRecycler = view.findViewById(R.id.reminderListRecycler);
        addReminderFab = view.findViewById(R.id.addReminderFab);

        // Sample reminders for testing
        reminderList = new ArrayList<>();
        reminderList.add(new Reminder("Vitamin C", "09:00 AM", true, "pill"));
        reminderList.add(new Reminder("Ibuprofen", "01:00 PM", false, "capsule"));
        reminderList.add(new Reminder("Magnesium", "06:00 PM", false, "drop"));

        // Set up adapter and recycler view
        reminderAdapter = new ReminderAdapter(reminderList);
        reminderListRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        reminderListRecycler.setAdapter(reminderAdapter);

        // Floating button action
        addReminderFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "Add Reminder clicked", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getContext(), AddReminderActivity.class));
            }
        });

        return view;
    }
}
