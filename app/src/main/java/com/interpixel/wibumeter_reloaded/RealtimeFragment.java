package com.interpixel.wibumeter_reloaded;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RadioGroup;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.Random;

import androidx.fragment.app.Fragment;

public class RealtimeFragment extends Fragment {

    private ImageButton startRealtimeButton;

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

        final RadioGroup radioCam = view.findViewById(R.id.radio_cam);

        startRealtimeButton = view.findViewById(R.id.startRealtime);
        startRealtimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Random random = new Random();
                if(random.nextBoolean() && random.nextBoolean()){
                    AdHelper.getAdHelper().ShowAds();
                    return;
                }

                Intent intent = new Intent(getActivity(), RealtimeDetectionActivity.class);
                if(radioCam.getCheckedRadioButtonId() == R.id.radio_front_cam){
                    intent.putExtra("front", true);
                }
                startActivity(intent);
            }
        });

        return view;
    }
}
