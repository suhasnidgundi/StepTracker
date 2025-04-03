package com.svcp.steptracker.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.svcp.steptracker.R;

public class SettingsActivity extends AppCompatActivity {

    private EditText goalEditText;
    private Switch notificationSwitch;
    private Switch darkModeSwitch;
    private Button saveButton;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        sharedPreferences = getSharedPreferences("step_tracker_prefs", MODE_PRIVATE);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Settings");

        // Initialize views
        goalEditText = findViewById(R.id.goal_edit_text);
        notificationSwitch = findViewById(R.id.notification_switch);
        darkModeSwitch = findViewById(R.id.dark_mode_switch);
        saveButton = findViewById(R.id.save_button);

        // Load settings
        loadSettings();

        // Set up save button
        saveButton.setOnClickListener(v -> saveSettings());
    }

    private void loadSettings() {
        // Load from SharedPreferences
        int dailyGoal = sharedPreferences.getInt("daily_goal", 10000);
        boolean notifications = sharedPreferences.getBoolean("notifications_enabled", true);
        boolean darkMode = sharedPreferences.getBoolean("dark_mode", false);

        // Set values to UI
        goalEditText.setText(String.valueOf(dailyGoal));
        notificationSwitch.setChecked(notifications);
        darkModeSwitch.setChecked(darkMode);

        // If user is logged in, try to load from Firestore as well
        if (currentUser != null) {
            db.collection("users").document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Long firebaseGoal = documentSnapshot.getLong("dailyGoal");
                            if (firebaseGoal != null) {
                                goalEditText.setText(String.valueOf(firebaseGoal));
                            }
                        }
                    });
        }
    }

    private void saveSettings() {
        // Validate inputs
        String goalString = goalEditText.getText().toString().trim();
        if (TextUtils.isEmpty(goalString)) {
            goalEditText.setError("Please enter a daily goal");
            return;
        }

        int goal;
        try {
            goal = Integer.parseInt(goalString);
            if (goal <= 0) {
                goalEditText.setError("Goal must be greater than zero");
                return;
            }
        } catch (NumberFormatException e) {
            goalEditText.setError("Please enter a valid number");
            return;
        }

        boolean notificationsEnabled = notificationSwitch.isChecked();
        boolean darkModeEnabled = darkModeSwitch.isChecked();

        // Save to SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("daily_goal", goal);
        editor.putBoolean("notifications_enabled", notificationsEnabled);
        editor.putBoolean("dark_mode", darkModeEnabled);
        editor.apply();

        // Save to Firestore if user is logged in
        if (currentUser != null) {
            db.collection("users").document(currentUser.getUid())
                    .update("dailyGoal", goal)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Settings saved successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to save settings to cloud", Toast.LENGTH_SHORT).show();
                        finish();
                    });
        } else {
            Toast.makeText(this, "Settings saved successfully", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}