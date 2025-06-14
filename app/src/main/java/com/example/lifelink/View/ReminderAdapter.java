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

    public interface OnReminderClickListener {
        void onEditClick(Reminder reminder);
        void onDeleteClick(Reminder reminder);
        void onMarkAsTakenClick(Reminder reminder); // ✅ Already added
    }

    private List<Reminder> reminderList;
    private OnReminderClickListener listener;

    public ReminderAdapter(List<Reminder> reminderList, OnReminderClickListener listener) {
        this.reminderList = reminderList;
        this.listener = listener;
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

        // ✅ Set bell icon based on taken status
        if (reminder.isTaken()) {
            holder.bellIcon.setImageResource(R.drawable.ic_check_green);

        } else {
            holder.bellIcon.setImageResource(R.drawable.ic_bell);
        }

        // ✅ Set days text
        if (reminder.getDays() != null && !reminder.getDays().isEmpty()) {
            StringBuilder daysBuilder = new StringBuilder();
            for (String day : reminder.getDays()) {
                switch (day) {
                    case "Mon": daysBuilder.append("M "); break;
                    case "Tue": daysBuilder.append("T "); break;
                    case "Wed": daysBuilder.append("W "); break;
                    case "Thu": daysBuilder.append("T "); break;
                    case "Fri": daysBuilder.append("F "); break;
                    case "Sat": daysBuilder.append("S "); break;
                    case "Sun": daysBuilder.append("S "); break;
                }
            }
            holder.daysText.setText(daysBuilder.toString().trim());
        } else {
            holder.daysText.setText("One-time");
        }

        // ✅ Edit click
        holder.editIcon.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(reminder);
            }
        });

        // ✅ Delete click
        holder.deleteIcon.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(reminder);
            }
        });

        // ✅ Mark as taken click
        holder.bellIcon.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMarkAsTakenClick(reminder);
            }
        });
    }

    @Override
    public int getItemCount() {
        return reminderList.size();
    }

    static class ReminderViewHolder extends RecyclerView.ViewHolder {
        TextView medName, medTime, daysText; // ✅ Added daysText here
        ImageView medIcon, bellIcon, editIcon, deleteIcon;

        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            medName = itemView.findViewById(R.id.medName);
            medTime = itemView.findViewById(R.id.medTime);
            daysText = itemView.findViewById(R.id.daysText); // ✅ Connect daysText
            medIcon = itemView.findViewById(R.id.medIcon);
            bellIcon = itemView.findViewById(R.id.bellIcon);
            editIcon = itemView.findViewById(R.id.editIcon);
            deleteIcon = itemView.findViewById(R.id.deleteIcon);
        }
    }
}
