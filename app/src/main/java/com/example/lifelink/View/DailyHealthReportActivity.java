package com.example.lifelink.View;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lifelink.Model.DailySummaryItem;
import com.example.lifelink.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DailyHealthReportActivity extends AppCompatActivity {

    private RecyclerView dailySummaryRecycler;
    private DailySummaryAdapter adapter;
    private List<DailySummaryItem> summaryList = new ArrayList<>();
    private TextView reportTitle;
    private String reportDateFormatted;
    private String reportDocDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_health_report);

        reportTitle = findViewById(R.id.reportTitle);
        dailySummaryRecycler = findViewById(R.id.dailySummaryRecycler);

        // Get date passed from intent
        reportDateFormatted = getIntent().getStringExtra("reportDate");
        if (reportDateFormatted == null) {
            reportDateFormatted = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            Toast.makeText(this, "⚠️ No date received. Defaulting to today.", Toast.LENGTH_SHORT).show();
        }

        reportDocDate = reportDateFormatted.replace("-", "_");
        reportTitle.setText("📅 Daily Health Report - " + reportDateFormatted);
        Toast.makeText(this, "Loading report for: " + reportDateFormatted, Toast.LENGTH_SHORT).show();

        dailySummaryRecycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DailySummaryAdapter(summaryList);
        dailySummaryRecycler.setAdapter(adapter);

        loadDailySummary();

        ImageView btnSaveReport = findViewById(R.id.btnSaveReport);
        ImageView btnShareReport = findViewById(R.id.btnShareReport);

        btnSaveReport.setOnClickListener(v -> {
            Toast.makeText(this, "Generating PDF report...", Toast.LENGTH_SHORT).show();
            generatePdf();
        });

        btnShareReport.setOnClickListener(v -> {
            Toast.makeText(this, "Preparing report for sharing...", Toast.LENGTH_SHORT).show();
            shareReportAsPdf();
        });
    }

    private void loadDailySummary() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (summaryList.isEmpty()) {
            Toast.makeText(this, "⚠️ No entries found for " + reportDateFormatted, Toast.LENGTH_LONG).show();
        }

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("daily_health_data")
                .document(reportDocDate)
                .collection("entries")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int totalHR = 0, totalSpO2 = 0, count = 0;
                    summaryList.clear();

                    List<DailySummaryItem> tempList = new ArrayList<>();

                    for (DocumentSnapshot doc : querySnapshot) {
                        Long hr = doc.getLong("heartRate");
                        Long spo2 = doc.getLong("spo2");
                        String time = doc.getString("timestamp");

                        if (hr != null && spo2 != null && time != null) {
                            totalHR += hr;
                            totalSpO2 += spo2;
                            count++;
                            tempList.add(new DailySummaryItem(time, hr.intValue(), spo2.intValue()));
                        }
                    }

                    // Sort the list based on timestamp (latest first)
                    tempList.sort((a, b) -> b.getTime().compareTo(a.getTime()));

                    if (count > 0) {
                        int avgHR = totalHR / count;
                        int avgSpO2 = totalSpO2 / count;
                        summaryList.add(new DailySummaryItem("Average", avgHR, avgSpO2));
                        Toast.makeText(this, "📈 " + count + " entries loaded.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "ℹ️ No health data found for this day.", Toast.LENGTH_SHORT).show();
                    }

                    summaryList.addAll(tempList);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "❌ Failed to load data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void generatePdf() {
        String fileName = "DailyHealthReport_" + reportDocDate + ".pdf";
        StringBuilder reportContent = new StringBuilder("📊 Daily Health Report for " + reportDateFormatted + "\n\n");

        for (DailySummaryItem summary : summaryList) {
            reportContent.append(summary.getTime())
                    .append(" - HR: ").append(summary.getHeartRate())
                    .append(" BPM, SpO₂: ").append(summary.getSpo2())
                    .append("%\n");
        }

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        Paint paint = new Paint();
        paint.setTextSize(14);
        int y = 50;
        for (String line : reportContent.toString().split("\n")) {
            canvas.drawText(line, 40, y, paint);
            y += 25;
        }

        document.finishPage(page);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            ContentResolver resolver = getContentResolver();
            Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

            try (OutputStream outputStream = resolver.openOutputStream(uri)) {
                document.writeTo(outputStream);
                document.close();
                Toast.makeText(this, "✅ PDF saved to Downloads", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                Toast.makeText(this, "❌ Error saving PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
            try (FileOutputStream out = new FileOutputStream(file)) {
                document.writeTo(out);
                document.close();
                Toast.makeText(this, "✅ Saved to: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                Toast.makeText(this, "❌ Error saving PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void shareReportAsPdf() {
        String fileName = "DailyHealthReport_" + reportDocDate + ".pdf";
        File file;

        // 1. Generate the PDF
        StringBuilder reportContent = new StringBuilder("📊 Daily Health Report for " + reportDateFormatted + "\n\n");
        for (DailySummaryItem summary : summaryList) {
            reportContent.append(summary.getTime())
                    .append(" - HR: ").append(summary.getHeartRate())
                    .append(" BPM, SpO₂: ").append(summary.getSpo2())
                    .append("%\n");
        }

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        Paint paint = new Paint();
        paint.setTextSize(14);
        int y = 50;
        for (String line : reportContent.toString().split("\n")) {
            canvas.drawText(line, 40, y, paint);
            y += 25;
        }

        document.finishPage(page);

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                // Scoped Storage: write to Downloads directory
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                ContentResolver resolver = getContentResolver();
                Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

                if (uri != null) {
                    try (OutputStream outputStream = resolver.openOutputStream(uri)) {
                        document.writeTo(outputStream);
                        document.close();

                        // Now share the PDF
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("application/pdf");
                        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(Intent.createChooser(shareIntent, "Share PDF via"));
                    }
                }
            } else {
                // For Android < Q
                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                file = new File(downloadsDir, fileName);
                try (FileOutputStream out = new FileOutputStream(file)) {
                    document.writeTo(out);
                    document.close();
                }

                Uri uri = Uri.fromFile(file);
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("application/pdf");
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(shareIntent, "Share PDF via"));
            }
        } catch (IOException e) {
            Toast.makeText(this, "❌ Error creating or sharing PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

}
