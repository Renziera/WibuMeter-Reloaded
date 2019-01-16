package com.interpixel.wibumeter_reloaded;

import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment;
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    fragment = new RealtimeFragment();
                    break;
                case R.id.navigation_history:
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

        getSupportActionBar().setIcon(R.drawable.ic_home_black_24dp);
        getSupportActionBar().setLogo(R.drawable.ic_dashboard_black_24dp);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new RealtimeFragment())
                .commit();
    }

}
