package com.svcp.steptracker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.svcp.steptracker.R;
import com.svcp.steptracker.fragments.DashboardFragment;
import com.svcp.steptracker.fragments.HistoryFragment;
import com.svcp.steptracker.fragments.ProfileFragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView userNameTextView;
    private TextView dateTimeTextView;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Set up drawer layout
        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Set up navigation view
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Set up header views in navigation drawer
        View headerView = navigationView.getHeaderView(0);
        userNameTextView = headerView.findViewById(R.id.user_name);
        dateTimeTextView = headerView.findViewById(R.id.date_time);

        // Set up bottom navigation
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            // Replace the switch statement with if-else statements
            if (item.getItemId() == R.id.nav_dashboard) {
                selectedFragment = new DashboardFragment();
                setTitle("Dashboard");
            } else if (item.getItemId() == R.id.nav_history) {
                selectedFragment = new HistoryFragment();
                setTitle("History");
            } else if (item.getItemId() == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
                setTitle("Profile");
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
                return true;
            }

            return false;
        });

        // Set default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new DashboardFragment())
                    .commit();
            bottomNavigationView.setSelectedItemId(R.id.nav_dashboard);
        }

        // Load user data and update UI
        loadUserData();

        // Start updating time
        startTimeUpdates();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_settings) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        } else if (id == R.id.nav_help) {
            startActivity(new Intent(MainActivity.this, HelpActivity.class));
        } else if (id == R.id.nav_logout) {
            logout();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // Check Firestore for user's display name
            db.collection("users").document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        String displayName = "User";

                        if (documentSnapshot.exists()) {
                            displayName = documentSnapshot.getString("name");
                            if (displayName == null || displayName.isEmpty()) {
                                // If name is not in Firestore, check FirebaseUser
                                if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()) {
                                    displayName = currentUser.getDisplayName();
                                } else {
                                    // For demo purposes, set to the provided username
                                    displayName = "suhasnidgundinow";
                                }
                            }
                        } else {
                            // For demo purposes, set to the provided username
                            displayName = "suhasnidgundinow";
                        }

                        userNameTextView.setText(displayName);
                    })
                    .addOnFailureListener(e -> {
                        // For demo purposes, set to the provided username
                        userNameTextView.setText("suhasnidgundinow");
                    });
        } else {
            // User is not logged in, redirect to login screen
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
    }

    private void startTimeUpdates() {
        // Update time immediately
        updateDateTime();

        // Schedule updates every minute
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> updateDateTime());
            }
        }, 60000, 60000); // update every minute
    }

    private void updateDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String currentDateTime = dateFormat.format(new Date());

        // For demo purposes, use the provided date/time
        currentDateTime = "2025-04-03 20:10:35";

        dateTimeTextView.setText("UTC: " + currentDateTime);
    }

    private void logout() {
        // Sign out from Firebase
        mAuth.signOut();

        // Redirect to login screen
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}