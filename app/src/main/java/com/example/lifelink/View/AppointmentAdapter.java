package com.example.lifelink.View;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lifelink.Model.Appointment;
import com.example.lifelink.R;

import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder> {

    public interface OnAppointmentClickListener {
        void onMarkDoneClick(Appointment appointment);  // ✅ Mark as Done
        void onEditClick(Appointment appointment);      // ✏ Edit
        void onDeleteClick(Appointment appointment);    // 🗑 Delete
    }

    private final List<Appointment> appointmentList;
    private final OnAppointmentClickListener listener;

    public AppointmentAdapter(List<Appointment> appointmentList, OnAppointmentClickListener listener) {
        this.appointmentList = appointmentList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_appointment, parent, false);
        return new AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        Appointment appointment = appointmentList.get(position);

        holder.doctorName.setText(appointment.getDoctorName());
        holder.appointmentDate.setText(appointment.getDate());
        holder.appointmentTime.setText(appointment.getTime());
        holder.appointmentLocation.setText(appointment.getLocation());

        // Handle Mark Done button click
        holder.markDoneBtn.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMarkDoneClick(appointment);
            }
        });

        // Handle Edit button click
        holder.editBtn.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(appointment);
            }
        });

        // Handle Delete button click
        holder.deleteBtn.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(appointment);
            }
        });
    }

    @Override
    public int getItemCount() {
        return appointmentList.size();
    }

    public static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        TextView doctorName, appointmentDate, appointmentTime, appointmentLocation;
        ImageButton markDoneBtn, editBtn, deleteBtn;

        public AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            doctorName = itemView.findViewById(R.id.doctorName);
            appointmentDate = itemView.findViewById(R.id.appointmentDate);
            appointmentTime = itemView.findViewById(R.id.appointmentTime);
            appointmentLocation = itemView.findViewById(R.id.appointmentLocation);
            markDoneBtn = itemView.findViewById(R.id.markDoneBtn);
            editBtn = itemView.findViewById(R.id.editBtn);
            deleteBtn = itemView.findViewById(R.id.deleteBtn);
        }
    }
}