package com.example.lifelink.View;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.lifelink.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class MedicalInfoBottomSheet extends BottomSheetDialogFragment {

    private String title;
    private String address;

    public static MedicalInfoBottomSheet newInstance(String title, String address) {
        MedicalInfoBottomSheet fragment = new MedicalInfoBottomSheet();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("address", address);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_medical_info, container, false);

        TextView titleText = view.findViewById(R.id.titleText);
        TextView addressText = view.findViewById(R.id.addressText);

        if (getArguments() != null) {
            title = getArguments().getString("title");
            address = getArguments().getString("address");
        }

        titleText.setText(title);
        addressText.setText(address);

        return view;
    }
}
