package com.example.lifelink.View;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.lifelink.Model.NearbyPlace;
import com.example.lifelink.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class NearbyPlaceDetailBottomSheet extends BottomSheetDialogFragment {

    private NearbyPlace place;

    public NearbyPlaceDetailBottomSheet(NearbyPlace place) {
        this.place = place;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.bottom_sheet_place_detail, container, false);

        TextView nameText = view.findViewById(R.id.placeNameText);
        TextView addressText = view.findViewById(R.id.placeAddressText);
        TextView phoneText = view.findViewById(R.id.placePhoneText);
        TextView emailText = view.findViewById(R.id.placeEmailText);
        TextView websiteText = view.findViewById(R.id.placeWebsiteText);
        Button searchWebButton = view.findViewById(R.id.searchWebButton);

        nameText.setText(place.getName());
        addressText.setText(place.getAddress());
        phoneText.setText(place.getPhone());
        emailText.setText(place.getEmail());
        websiteText.setText(place.getWebsite());

        // Handle Phone Click
        phoneText.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(place.getPhone()) && !place.getPhone().contains("not available")) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + place.getPhone()));
                startActivity(intent);
            }
        });

        // Handle Email Click
        emailText.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(place.getEmail()) && !place.getEmail().contains("not available")) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:" + place.getEmail()));
                startActivity(intent);
            }
        });

        // Handle Website Click
        websiteText.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(place.getWebsite()) && !place.getWebsite().contains("not available")) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(place.getWebsite()));
                startActivity(intent);
            }
        });

        // Check if all info is not available -> show "Search More on Web" button
        if ((place.getPhone() == null || place.getPhone().contains("Not available"))
               || (place.getEmail() == null || place.getEmail().contains("Not available"))
                || (place.getWebsite() == null || place.getWebsite().contains("Not available"))) {

            searchWebButton.setVisibility(View.VISIBLE);
            searchWebButton.setOnClickListener(v -> {
                String query = place.getName() + " Lebanon";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + Uri.encode(query)));
                startActivity(intent);
            });
        }

        return view;
    }
}
