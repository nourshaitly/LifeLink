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

    private final int[] images = {
            R.drawable.welcome_1,
            R.drawable.welcome_2,
            R.drawable.welcome_3
    };

    private final String[] titles = {
            "Welcome to LifeLink",
            "Track Your Health",
            "Stay Connected"
    };

    private final String[] descriptions = {
            "Your personal health companion for monitoring vital signs and staying healthy.",
            "Monitor your heart rate, SPO2 levels, and other vital signs in real-time.",
            "Connect with healthcare providers and get instant health insights."
    };

    @NonNull
    @Override
    public SlideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.welcome_slide_item, parent, false);
        return new SlideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SlideViewHolder holder, int position) {
        holder.imageView.setImageResource(images[position]);
        holder.titleView.setText(titles[position]);
        holder.descriptionView.setText(descriptions[position]);
    }

    @Override
    public int getItemCount() {
        return images.length;
    }

    static class SlideViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleView;
        TextView descriptionView;

        SlideViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.slideImage);
            titleView = itemView.findViewById(R.id.slideTitle);
            descriptionView = itemView.findViewById(R.id.slideDescription);
        }
    }
} 