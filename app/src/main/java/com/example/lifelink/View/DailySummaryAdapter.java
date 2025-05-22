package com.example.lifelink.View;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lifelink.Model.DailySummaryItem;
import com.example.lifelink.R;

import java.util.List;

public class DailySummaryAdapter extends RecyclerView.Adapter<DailySummaryAdapter.SummaryViewHolder> {

    private final List<DailySummaryItem> summaryList;

    public DailySummaryAdapter(List<DailySummaryItem> summaryList) {
        this.summaryList = summaryList;
    }

    @NonNull
    @Override
    public SummaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_daily_summary, parent, false);
        return new SummaryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SummaryViewHolder holder, int position) {
        DailySummaryItem item = summaryList.get(position);

        holder.timeText.setText(item.getTime());
        holder.heartRateText.setText(item.getHeartRate() + " BPM");
        holder.spo2Text.setText(item.getSpo2() + "%");

        // Highlight the average row
        if ("Average".equals(item.getTime())) {
            holder.timeText.setText("\uD83D\uDCC5 " + item.getTime());
            holder.timeText.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.purple_700));
            holder.heartRateText.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.purple_700));
            holder.spo2Text.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.purple_700));
        }
    }

    @Override
    public int getItemCount() {
        return summaryList.size();
    }

    static class SummaryViewHolder extends RecyclerView.ViewHolder {
        TextView timeText, heartRateText, spo2Text;

        public SummaryViewHolder(@NonNull View itemView) {
            super(itemView);
            timeText = itemView.findViewById(R.id.dateText);
            heartRateText = itemView.findViewById(R.id.avgHeartRate);
            spo2Text = itemView.findViewById(R.id.avgSpo2);
        }
    }
}
