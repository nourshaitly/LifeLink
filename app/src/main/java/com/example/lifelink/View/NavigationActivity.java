package com.example.lifelink.View;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.MenuItem;
import android.widget.FrameLayout;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.lifelink.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class NavigationActivity extends AppCompatActivity {

    private static final int CALL_PERMISSION_REQUEST = 100;
    private final String sosnum = "+96171010584";

    /** Maps each nav‐item ID to the Activity class it should launch. */
    private static final SparseArray<Class<?>> NAV_MAP = new SparseArray<>();
    static {
        NAV_MAP.put(R.id.nav_nearby,   MapIntroActivity.class);
        NAV_MAP.put(R.id.nav_reminder, RemindersWelcomeActivity.class);
        NAV_MAP.put(R.id.nav_home,     MainPageActivity.class);
        NAV_MAP.put(R.id.nav_emergency, EmergencyActivity.class);
        NAV_MAP.put(R.id.nav_health,    HealthTrackerActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Do not call setContentView() here—
        // subclasses will call it and our override will wire everything up.
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        // 1) Inflate the shared chrome
        super.setContentView(R.layout.activity_navigation);

        // 2) Inject the child's layout into the placeholder
        FrameLayout container = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(layoutResID, container, true);

        // 3) Wire toolbar, SOS FAB, BottomNav
        setupToolbar();
        setupSOSFab();
        setupBottomNav();
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Show the Up arrow if desired
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setupSOSFab() {
        FloatingActionButton fab = findViewById(R.id.fab_sos);
        fab.setOnClickListener(v -> triggerCall());
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
       // bottomNav.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_UNLABELED);
        bottomNav.setOnItemSelectedListener(this::onNavItemSelected);
        // Default highlight “Home”
        bottomNav.setSelectedItemId(R.id.nav_home);
    }

    private boolean onNavItemSelected(@NonNull MenuItem item) {
        Class<?> target = NAV_MAP.get(item.getItemId());
        if (target == null) {
            // no matching entry
            return false;
        }
        Intent intent = new Intent(this, target)
                .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle toolbar Up arrow
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void triggerCall() {
        Intent callIntent = new Intent(Intent.ACTION_CALL,
                Uri.parse("tel:" + sosnum));
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            startActivity(callIntent);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE},
                    CALL_PERMISSION_REQUEST);
        }
    }
}
