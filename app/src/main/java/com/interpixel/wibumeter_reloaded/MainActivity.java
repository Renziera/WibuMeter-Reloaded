package com.interpixel.wibumeter_reloaded;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment;
            switch (item.getItemId()) {
                case R.id.navigation_realtime:
                    fragment = new RealtimeFragment();
                    break;
                case R.id.navigation_photo:
                    fragment = new PhotoFragment();
                    break;
                case R.id.navigation_about:
                    fragment = new AboutFragment();
                    break;
                default:
                    fragment = new RealtimeFragment();
                    break;
            }
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setIcon(R.drawable.ic_photo_camera_black_24dp);
        getSupportActionBar().setLogo(R.drawable.ic_photo_camera_black_24dp);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new RealtimeFragment())
                .commit();

        setDivergence();
    }

    private void setDivergence(){

        Random random = new Random();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String value = sp.getString("divergence", "");

        if(value.isEmpty()){    //Pertama kali buka app
            StringBuilder sb = new StringBuilder();
            sb.append("0 . 1 7 3 ");
            sb.append(random.nextInt(10));
            sb.append(" ");
            sb.append(random.nextInt(10));
            sb.append(" ");
            sb.append(random.nextInt(10));
            sb.append(" ");
            sb.append(random.nextInt(10));
            value = sb.toString();
        }else{      //randomly changes last two digits every time app starts
            if(random.nextBoolean() && random.nextBoolean()){
                StringBuilder sb = new StringBuilder(value);
                sb.replace(14, 17, random.nextInt(10) + " " + random.nextInt(10));
                value = sb.toString();
            }else if(random.nextBoolean()){
                StringBuilder sb = new StringBuilder(value);
                sb.replace(16, 17, random.nextInt(10) + "");
                value = sb.toString();            }
        }

        sp.edit().putString("divergence", value).apply();
    }

}
