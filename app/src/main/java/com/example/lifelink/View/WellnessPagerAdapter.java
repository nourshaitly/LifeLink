package com.example.lifelink.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class WellnessPagerAdapter extends FragmentStateAdapter {

    public WellnessPagerAdapter(@NonNull FragmentActivity fa) {
        super(fa);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 1:
                return new TipsOfTodayFragment();

            default:
                return new WellnessFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
