package com.example.lifelink.View;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;

import com.example.lifelink.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class WellnessFragment extends Fragment {

    private LinearLayout introSection, setupSection;
    private Button btnStartSetup, btnStartTips, btnStopTips;
    private EditText editFrequencyNumber;
    private Spinner spinnerFrequencyUnit;

    private final Handler tipHandler = new Handler();
    private Runnable tipRunnable;

    private static final String CHANNEL_ID = "wellness_tips_channel";
    private int tipNotificationId = 1;

    private final Map<String, List<String>> wellnessTips = new HashMap<>();
    private final List<String> selectedCategories = new ArrayList<>();

    private final Map<String, Button> categoryButtons = new HashMap<>();
    private final Map<String, String> categoryEmojis = new HashMap<String, String>() {{
        put("Hydration", "💧");
        put("Nutrition", "🍎");
        put("Mental Health", "🧘");
        put("Sleep", "🌙");
        put("Movement", "🚶");
        put("Motivation", "💬");
    }};

    public WellnessFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wellness, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        introSection = view.findViewById(R.id.introSection);
        setupSection = view.findViewById(R.id.setupSection);
        btnStartSetup = view.findViewById(R.id.btnStartSetup);
        btnStartTips = view.findViewById(R.id.btnStartTips);
        btnStopTips = view.findViewById(R.id.btnStopTips);
        editFrequencyNumber = view.findViewById(R.id.editFrequencyNumber);
        spinnerFrequencyUnit = view.findViewById(R.id.spinnerFrequencyUnit);

        // Load tips from JSON
        wellnessTips.putAll(loadTipsFromJson(requireContext()));

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.frequency_units,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrequencyUnit.setAdapter(adapter);

        btnStartSetup.setOnClickListener(v -> {
            fadeOutView(introSection);
            fadeInView(setupSection);
        });

        setupCategoryButtons(view);

        btnStartTips.setOnClickListener(v -> {
            String numberText = editFrequencyNumber.getText().toString().trim();
            if (numberText.isEmpty() || Integer.parseInt(numberText) <= 0) {
                Toast.makeText(getContext(), "Please enter a valid number", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedCategories.isEmpty()) {
                Toast.makeText(getContext(), "Please select at least one category", Toast.LENGTH_SHORT).show();
                return;
            }

            int freq = Integer.parseInt(numberText);
            String unit = spinnerFrequencyUnit.getSelectedItem().toString();
            long intervalMillis = getIntervalMillis(freq, unit);

            if (intervalMillis <= 0) {
                Toast.makeText(getContext(), "Invalid interval selected", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(getContext(), "Tips will appear every " + freq + " " + unit.toLowerCase(), Toast.LENGTH_SHORT).show();
            startRepeatingTips(intervalMillis);
        });

        btnStopTips.setOnClickListener(v -> {
            if (tipRunnable != null) {
                tipHandler.removeCallbacks(tipRunnable);
                Toast.makeText(getContext(), "Tips stopped.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupCategoryButtons(View view) {
        addCategoryButton(view, R.id.btnHydration, "Hydration");
        addCategoryButton(view, R.id.btnNutrition, "Nutrition");
        addCategoryButton(view, R.id.btnMental, "Mental Health");
        addCategoryButton(view, R.id.btnSleep, "Sleep");
        addCategoryButton(view, R.id.btnMovement, "Movement");
        addCategoryButton(view, R.id.btnMotivation, "Motivation");
    }

    private void addCategoryButton(View view, int buttonId, String category) {
        Button button = view.findViewById(buttonId);
        categoryButtons.put(category, button);
        button.setOnClickListener(v -> toggleCategory(category));
    }

    private void toggleCategory(String category) {
        Button button = categoryButtons.get(category);
        if (button == null) return;

        if (selectedCategories.contains(category)) {
            selectedCategories.remove(category);
            button.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
            button.setText(categoryEmojis.get(category) + " " + category);
            button.setTextColor(Color.BLACK);
        } else {
            selectedCategories.add(category);
            button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#A5D6A7")));
            button.setText("✅ " + category);
            button.setTextColor(Color.BLACK);
        }
    }

    private Map<String, List<String>> loadTipsFromJson(Context context) {
        try {
            InputStream is = context.getAssets().open("wellness_tips.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String json = new String(buffer, StandardCharsets.UTF_8);
            Type type = new TypeToken<Map<String, List<String>>>() {}.getType();

            return new Gson().fromJson(json, type);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Failed to load tips.", Toast.LENGTH_SHORT).show();
            return new HashMap<>();
        }
    }

    private void startRepeatingTips(long intervalMillis) {
        List<String> selectedTips = new ArrayList<>();
        for (String category : selectedCategories) {
            List<String> tips = wellnessTips.get(category);
            if (tips != null) selectedTips.addAll(tips);
        }

        if (selectedTips.isEmpty()) {
            Toast.makeText(getContext(), "No tips available for selected categories.", Toast.LENGTH_SHORT).show();
            return;
        }

        tipRunnable = new Runnable() {
            @Override
            public void run() {
                String tip = selectedTips.get(new Random().nextInt(selectedTips.size()));
                showTipNotification(tip);
                tipHandler.postDelayed(this, intervalMillis);
            }
        };

        tipHandler.post(tipRunnable);
    }

    private void showTipNotification(String tip) {
        if (getContext() == null) return;

        NotificationManager manager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Wellness Tips",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Personalized wellness tips");
            channel.enableVibration(true);
            channel.enableLights(true);
            manager.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_health)
                .setContentTitle("💡 Wellness Tip")
                .setContentText(tip)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_ALL)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .build();

        manager.notify(tipNotificationId++, notification);
    }

    private long getIntervalMillis(int value, String unit) {
        switch (unit) {
            case "Minutes":
                return TimeUnit.MINUTES.toMillis(value);
            case "Hours":
                return TimeUnit.HOURS.toMillis(value);
            case "Days":
                return TimeUnit.DAYS.toMillis(value);
            default:
                return 0;
        }
    }

    private void fadeOutView(View view) {
        view.animate()
                .alpha(0f)
                .setDuration(500)
                .withEndAction(() -> view.setVisibility(View.GONE))
                .start();
    }

    private void fadeInView(View view) {
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
                .alpha(1f)
                .setDuration(500)
                .start();
    }
}
