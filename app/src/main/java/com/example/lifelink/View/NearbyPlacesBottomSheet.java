
package com.example.lifelink.View;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lifelink.Model.NearbyPlace;
import com.example.lifelink.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

public class NearbyPlacesBottomSheet extends BottomSheetDialogFragment {


    private List<NearbyPlace> hospitals;
    private List<NearbyPlace> pharmacies;
    private OnPlaceClickListener listener;

    public interface OnPlaceClickListener {
        void onPlaceClick(NearbyPlace place);
    }

    public NearbyPlacesBottomSheet(List<NearbyPlace> hospitals, List<NearbyPlace> pharmacies, OnPlaceClickListener listener) {
        this.hospitals = hospitals;
        this.pharmacies = pharmacies;
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_nearby_places, container, false);

        LinearLayout hospitalSection = view.findViewById(R.id.hospitalSection);
        LinearLayout pharmacySection = view.findViewById(R.id.pharmacySection);

        RecyclerView hospitalRecyclerView = view.findViewById(R.id.hospitalsRecyclerView);
        RecyclerView pharmacyRecyclerView = view.findViewById(R.id.pharmaciesRecyclerView);

        hospitalRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        pharmacyRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Hospitals
        if (hospitals != null && !hospitals.isEmpty()) {
            hospitalRecyclerView.setAdapter(new PlacesAdapter(hospitals));
            hospitalSection.setVisibility(View.VISIBLE);
        } else {
            hospitalSection.setVisibility(View.GONE);
        }

        // Pharmacies
        if (pharmacies != null && !pharmacies.isEmpty()) {
            pharmacyRecyclerView.setAdapter(new PlacesAdapter(pharmacies));
            pharmacySection.setVisibility(View.VISIBLE);
        } else {
            pharmacySection.setVisibility(View.GONE);
        }

        return view;
    }

    // ----------------- Adapter ------------------
    private class PlacesAdapter extends RecyclerView.Adapter<PlacesAdapter.ViewHolder> {

        private List<NearbyPlace> places;

        public PlacesAdapter(List<NearbyPlace> places) {
            this.places = places;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_nearby_place, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            NearbyPlace place = places.get(position);

            holder.placeName.setText(place.getName());
            holder.placeAddress.setText(place.getAddress());
            holder.placeDistance.setText(String.format("%.0f meters away", place.getDistance()));

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPlaceClick(place);
                    dismiss();
                }
            });
        }

        @Override
        public int getItemCount() {
            return places.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            TextView placeName, placeAddress, placeDistance;
            CardView cardView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                placeName = itemView.findViewById(R.id.placeName);
                placeAddress = itemView.findViewById(R.id.placeAddress);
                placeDistance = itemView.findViewById(R.id.placeDistance);
                cardView = (CardView) itemView;
            }
        }
    }

}