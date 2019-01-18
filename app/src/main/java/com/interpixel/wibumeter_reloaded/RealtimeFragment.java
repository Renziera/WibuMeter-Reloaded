package com.interpixel.wibumeter_reloaded;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import androidx.fragment.app.Fragment;

public class RealtimeFragment extends Fragment {

    private Button testButton;

    public RealtimeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_realtime, container, false);

        AdView adView1 = view.findViewById(R.id.adViewRealtime1);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView1.loadAd(adRequest);

        AdView adView2 = view.findViewById(R.id.adViewRealtime2);
        adView2.loadAd(new AdRequest.Builder().build());

        testButton = view.findViewById(R.id.testButton);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), RealtimeDetectionActivity.class);
                startActivity(intent);
            }
        });

        Button frontcam = view.findViewById(R.id.frontcam);
        frontcam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), RealtimeDetectionActivity.class);
                intent.putExtra("front", true);
                startActivity(intent);
            }
        });

        return view;
    }
}
