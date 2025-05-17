package com.example.lifelink.View;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.viewpager2.widget.ViewPager2;
import com.example.lifelink.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class WellnessDashboardActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private WellnessPagerAdapter pagerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wellness_dashboard);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        pagerAdapter = new WellnessPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position) {
                    case 0:
                        tab.setText("Wellness");
                        break;
                    case 1:
                        tab.setText("Today's Tips");
                        break;

                }
            }
        }).attach();

        int idx = getIntent().getIntExtra("EXTRA_TAB_INDEX", -1);
        if (idx >= 0 && idx < pagerAdapter.getItemCount()) {
            viewPager.setCurrentItem(idx, false);
        }
        DashboardUtils.init(this,R.id.bottomNavigationView);


    }


    // Also add this method below onCreate:
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        int idx = intent.getIntExtra("EXTRA_TAB_INDEX", -1);
        if (idx >= 0 && idx < pagerAdapter.getItemCount()) {
            viewPager.setCurrentItem(idx, false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // let DashboardUtils handle the Up/Home click
        if (DashboardUtils.onHomeClicked(this, item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}