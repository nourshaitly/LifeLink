package com.example.lifelink.View;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.lifelink.Model.Tip;
import com.example.lifelink.R;
import com.example.lifelink.View.TipRepository;

import java.util.List;

public class TipsOfTodayFragment extends Fragment {

    private LinearLayout tipsContainer;

    public TipsOfTodayFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tips_of_today, container, false);
        tipsContainer = view.findViewById(R.id.tipsContainer);

        // 1) Grab today’s recorded tips
        List<Tip> tips = TipRepository.getTips();

        // 2) If empty, show a “no tips yet” placeholder
        if (tips.isEmpty()) {
            View empty = inflater.inflate(R.layout.view_no_tips, tipsContainer, false);
            tipsContainer.addView(empty);
            return view;
        }

        // 3) Inflate a card for each tip (newest first)
        for (Tip tip : tips) {
            View tipCard = inflater.inflate(R.layout.item_tip_card, tipsContainer, false);
            TextView titleView = tipCard.findViewById(R.id.tipTitle);
            TextView textView  = tipCard.findViewById(R.id.tipText);

            titleView.setText(tip.title);
            textView .setText(tip.text);

            tipsContainer.addView(tipCard);
        }

        return view;
    }
}
