package com.example.lifelink.View;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lifelink.Model.NearbyPlace;

import java.util.List;

public class NearbyPlacesAdapter extends RecyclerView.Adapter<NearbyPlacesAdapter.ViewHolder> {

private List<NearbyPlace> places;
@Nullable
private OnPlaceClickListener listener;

public interface OnPlaceClickListener {
    void onPlaceClick(NearbyPlace place);
}

public NearbyPlacesAdapter(List<NearbyPlace> places) {
    this.places = places;
    this.listener = null;
}

public NearbyPlacesAdapter(List<NearbyPlace> places, OnPlaceClickListener listener) {
    this.places = places;
    this.listener = listener;
}

@NonNull
@Override
public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
            .inflate(android.R.layout.simple_list_item_1, parent, false);
    return new ViewHolder(view);
}

@Override
public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    NearbyPlace place = places.get(position);

    String distanceText;
    if (place.getDistance() < 1000) {
        distanceText = String.format(" (%.0f m)", place.getDistance());
    } else {
        distanceText = String.format(" (%.1f km)", place.getDistance() / 1000f);
    }

    holder.textView.setText(place.getName() + distanceText);

    if (listener != null) {
        holder.itemView.setOnClickListener(v -> listener.onPlaceClick(place));
    }
}

@Override
public int getItemCount() {
    return places.size();
}

public static class ViewHolder extends RecyclerView.ViewHolder {
    TextView textView;

    public ViewHolder(@NonNull View itemView) {
        super(itemView);
        textView = itemView.findViewById(android.R.id.text1);
    }
}
}