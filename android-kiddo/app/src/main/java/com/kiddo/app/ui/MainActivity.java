package com.kiddo.app.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.kiddo.app.R;
import com.kiddo.app.utils.AppState;
import com.kiddo.app.utils.StorageManager;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StorageManager.init(getApplicationContext());
        AppState.init(getApplicationContext());
        if (savedInstanceState == null) {
            Fragment start = AppState.get().hasActiveSession() ? new HomeFragment() : new AuthFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, start)
                    .commit();
        }
    }
}
