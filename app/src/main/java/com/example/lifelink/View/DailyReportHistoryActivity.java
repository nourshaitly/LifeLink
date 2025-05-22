package com.example.lifelink.View;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lifelink.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DailyReportHistoryActivity extends AppCompatActivity implements ReportDateAdapter.OnDateClickListener {

    private RecyclerView recyclerView;
    private ReportDateAdapter adapter;
    private List<String> reportDates = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_report_history);

        recyclerView = findViewById(R.id.recyclerReportDates);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReportDateAdapter(reportDates, this);
        recyclerView.setAdapter(adapter);

        fetchReportDates();
    }

    private void fetchReportDates() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        CollectionReference dailyDataRef = FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("daily_health_data");

        dailyDataRef.get().addOnSuccessListener(querySnapshot -> {
            reportDates.clear();
            int count = 0;
            for (QueryDocumentSnapshot doc : querySnapshot) {
                String formattedDate = doc.getId().replace("_", "-");
                reportDates.add(formattedDate);
                count++;
            }
            Collections.sort(reportDates, Collections.reverseOrder()); // Show latest first
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "✅ Loaded " + count + " report day(s)", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e ->
                Toast.makeText(this, "❌ Failed to load report dates", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDateClicked(String dateFormatted) {
        Intent intent = new Intent(this, DailyHealthReportActivity.class);
        intent.putExtra("reportDate", dateFormatted);
        startActivity(intent);
    }
}
