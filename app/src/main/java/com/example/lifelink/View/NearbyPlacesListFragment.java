package com.example.lifelink.View;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.lifelink.Adapter.NearbyPlacesAdapter;
import com.example.lifelink.Model.NearbyPlace;
import com.example.lifelink.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class NearbyPlacesListFragment extends Fragment {

    private static final String ARG_PLACES = "places";

    private List<NearbyPlace> places;
    private NearbyPlacesBottomSheet.OnPlaceClickListener listener;

    public static NearbyPlacesListFragment newInstance(List<NearbyPlace> places, NearbyPlacesBottomSheet.OnPlaceClickListener listener) {
        NearbyPlacesListFragment fragment = new NearbyPlacesListFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PLACES, new ArrayList<>(places));
        fragment.setArguments(args);
        fragment.listener = listener;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            places = (List<NearbyPlace>) getArguments().getSerializable(ARG_PLACES);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nearby_places_list, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Adapter → shared one
        NearbyPlacesAdapter adapter = new NearbyPlacesAdapter(places, (NearbyPlacesAdapter.OnPlaceClickListener) listener);
        recyclerView.setAdapter(adapter);

        return view;
    }


}