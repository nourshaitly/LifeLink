package com.example.lifelink.View;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lifelink.R;

import java.util.List;

// ReportDateAdapter.java
public class ReportDateAdapter extends RecyclerView.Adapter<ReportDateAdapter.DateViewHolder> {

    public interface OnDateClickListener {
        void onDateClicked(String dateFormatted);
    }

    private final List<String> dateList;
    private final OnDateClickListener listener;

    public ReportDateAdapter(List<String> dateList, OnDateClickListener listener) {
        this.dateList = dateList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report_date, parent, false);
        return new DateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DateViewHolder holder, int position) {
        String formattedDate = dateList.get(position);
        holder.dateText.setText("📅 " + formattedDate);

        holder.itemView.setOnClickListener(v -> listener.onDateClicked(formattedDate));
    }

    @Override
    public int getItemCount() {
        return dateList.size();
    }

    static class DateViewHolder extends RecyclerView.ViewHolder {
        TextView dateText;

        public DateViewHolder(@NonNull View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.reportDateText);

        }
    }
}
