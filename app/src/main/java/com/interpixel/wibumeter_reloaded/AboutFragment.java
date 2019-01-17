package com.interpixel.wibumeter_reloaded;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class AboutFragment extends Fragment {

    public AboutFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        TextView divergence = view.findViewById(R.id.divergence);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        String value = sp.getString("divergence", "0 . 1 7 3 0 0 3 4");
        divergence.setText(value);

        return view;
    }
}
