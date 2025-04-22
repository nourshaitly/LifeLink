package com.example.lifelink.View;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lifelink.Model.Reminder;
import com.example.lifelink.R;

import java.util.List;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {

    private List<Reminder> reminderList;

    public ReminderAdapter(List<Reminder> reminderList) {
        this.reminderList = reminderList;
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reminder, parent, false);
        return new ReminderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        Reminder reminder = reminderList.get(position);
        holder.medName.setText(reminder.getName());
        holder.medTime.setText(reminder.getTime());

        // Optional: Update icons or visual cues based on state
        if (reminder.isTaken()) {
            holder.bellIcon.setVisibility(View.GONE); // Example: hide bell if already taken
        } else {
            holder.bellIcon.setVisibility(View.VISIBLE);
        }

        // Optional: Change icon dynamically based on type
        if (reminder.getType().equalsIgnoreCase("drop")) {
            holder.medIcon.setImageResource(R.drawable.ic_drop);
        } else if (reminder.getType().equalsIgnoreCase("capsule")) {
            holder.medIcon.setImageResource(R.drawable.ic_capsule);
        } else {
            holder.medIcon.setImageResource(R.drawable.ic_pill); // default
        }
    }

    @Override
    public int getItemCount() {
        return reminderList.size();
    }

    static class ReminderViewHolder extends RecyclerView.ViewHolder {
        TextView medName, medTime;
        ImageView medIcon, bellIcon, editIcon;

        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            medName = itemView.findViewById(R.id.medName);
            medTime = itemView.findViewById(R.id.medTime);
            medIcon = itemView.findViewById(R.id.medIcon);
            bellIcon = itemView.findViewById(R.id.bellIcon);
            editIcon = itemView.findViewById(R.id.editIcon);
        }
    }
}
