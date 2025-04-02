package com.example.lifelink.View;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lifelink.R;
import java.util.List;

public class slideAdapter extends RecyclerView.Adapter<slideAdapter.SlideViewHolder> {

    private final List<slideItem> slideItems;

    public slideAdapter(List<slideItem> slideItems) {
        this.slideItems = slideItems;
    }

    @NonNull
    @Override
    public SlideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.slide_item, parent, false);
        return new SlideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SlideViewHolder holder, int position) {
        slideItem slide = slideItems.get(position);
        holder.imageView.setImageResource(slide.getImageRes());


    }

    @Override
    public int getItemCount() {
        return slideItems.size();
    }

    public static class SlideViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public SlideViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageSlide);

        }
    }
}
