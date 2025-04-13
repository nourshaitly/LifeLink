package com.example.lifelink.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lifelink.R;

public class WelcomeSlideAdapter extends RecyclerView.Adapter<WelcomeSlideAdapter.SlideViewHolder> {

    private static final int[] SLIDE_IMAGES = {
            R.drawable.lifelinkim,
            R.drawable.lifelinkim,
            R.drawable.lifelinkim,
    };

    private static final String[] SLIDE_TITLES = {
            "Welcome to LifeLink",
            "Your Health Companion",
            "Get Started"
    };

    private static final String[] SLIDE_DESCRIPTIONS = {
            "Track your health and lifestyle with ease",
            "Monitor your daily activities and health metrics",
            "Join our community and start your health journey"
    };

    @NonNull
    @Override
    public SlideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_welcome_slide, parent, false);
        return new SlideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SlideViewHolder holder, int position) {
        holder.slideImage.setImageResource(SLIDE_IMAGES[position]);
        holder.slideTitle.setText(SLIDE_TITLES[position]);
        holder.slideDescription.setText(SLIDE_DESCRIPTIONS[position]);
    }

    @Override
    public int getItemCount() {
        return SLIDE_IMAGES.length;
    }

    static class SlideViewHolder extends RecyclerView.ViewHolder {
        ImageView slideImage;
        TextView slideTitle;
        TextView slideDescription;

        SlideViewHolder(@NonNull View itemView) {
            super(itemView);
            slideImage = itemView.findViewById(R.id.slideImage);
            slideTitle = itemView.findViewById(R.id.slideTitle);
            slideDescription = itemView.findViewById(R.id.slideDescription);
        }
    }
} 