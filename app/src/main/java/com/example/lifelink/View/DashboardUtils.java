package com.example.lifelink.View;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.MenuItem;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.lifelink.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import android.util.SparseArray;

public class DashboardUtils {
    private static final String SOS_NUM = "+96171010584";
    private static final int CALL_PERM_REQ = 100;

    // map bottom-nav IDs to Activity classes
    private static final SparseArray<Class<?>> NAV_MAP = new SparseArray<>();
    static {
        NAV_MAP.put(R.id.nav_nearby,    MapIntroActivity.class);
        NAV_MAP.put(R.id.nav_reminder,  RemindersWelcomeActivity.class);
        NAV_MAP.put(R.id.nav_home,      AIChatActivity.class);
        NAV_MAP.put(R.id.nav_emergency, EmergencyActivity.class);
        NAV_MAP.put(R.id.nav_health,    HealthTrackerActivity.class);
    }

    /**
     * Initialize toolbar back button, profile icon, SOS FAB, and bottom navigation.
     *
     * Call this in your Activity.onCreate() immediately after setContentView(...).
     *
     * @param act                 the AppCompatActivity
     * @param selectedNavItemId   the menu-item id to highlight in bottom nav
     */
    public static void init(final AppCompatActivity act, @IdRes int selectedNavItemId) {
        // Toolbar
        MaterialToolbar toolbar = act.findViewById(R.id.toolbar);
        act.setSupportActionBar(toolbar);
        act.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Profile icon (assumes ImageView with id profileIcon)
        act.findViewById(R.id.profileIcon)
                .setOnClickListener(v -> act.startActivity(
                        new Intent(act, ProfileActivity.class)
                ));

        // SOS FloatingActionButton
        ExtendedFloatingActionButton fab = act.findViewById(R.id.fab_sos);
        fab.setOnClickListener(v -> triggerCall(act));

        // BottomNavigationView
        BottomNavigationView nav = act.findViewById(R.id.bottomNavigationView);
        nav.setOnItemSelectedListener(item -> {
            Class<?> target = NAV_MAP.get(item.getItemId());
            if (target == null) return false;
            Intent i = new Intent(act, target)
                    .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                            | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            act.startActivity(i);
            act.overridePendingTransition(0, 0);
            return true;
        });
        // highlight the correct tab
        nav.setSelectedItemId(selectedNavItemId);
    }

    /**
     * Call from your Activity.onOptionsItemSelected to handle toolbar-Up.
     *
     * @return true if handled here, false to let super handle.
     */
    public static boolean onHomeClicked(AppCompatActivity act, MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // navigate to the "home" tab (MainPageActivity)
            Intent i = new Intent(act, NAV_MAP.get(R.id.nav_home))
                    .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                            | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            act.startActivity(i);
            act.overridePendingTransition(0, 0);
            return true;
        }
        return false;
    }

    // helper to place a call (SOS)
    private static void triggerCall(Activity act) {
        Intent call = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + SOS_NUM));
        if (ActivityCompat.checkSelfPermission(act, Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED) {
            act.startActivity(call);
        } else {
            ActivityCompat.requestPermissions(
                    act,
                    new String[]{ Manifest.permission.CALL_PHONE },
                    CALL_PERM_REQ
            );
        }
    }
}
