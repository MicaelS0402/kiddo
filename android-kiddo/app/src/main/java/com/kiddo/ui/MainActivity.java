package com.kiddo.ui;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.kiddo.R;
import com.kiddo.utils.Storage;
import com.kiddo.ui.auth.AuthFragment;
import com.kiddo.ui.home.HomeFragment;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_logout) {
                Storage.clearSession(this);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, new AuthFragment())
                        .commit();
            } else if (id == R.id.menu_reset) {
                HomeFragment f = getHomeFragment();
                if (f != null) f.resetProgress();
            } else if (id == R.id.menu_export) {
                HomeFragment f = getHomeFragment();
                if (f != null) f.exportData();
            } else if (id == R.id.menu_account) {
                HomeFragment f = getHomeFragment();
                if (f != null) f.openAccount();
            } else if (id == R.id.menu_progress) {
                HomeFragment f = getHomeFragment();
                if (f != null) f.openProgress();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        if (savedInstanceState == null) {
            if (Storage.checkSession(this) != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, new HomeFragment())
                        .commit();
            } else {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, new AuthFragment())
                        .commit();
            }
        }
    }

    private HomeFragment getHomeFragment() {
        return (HomeFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
    }
}
