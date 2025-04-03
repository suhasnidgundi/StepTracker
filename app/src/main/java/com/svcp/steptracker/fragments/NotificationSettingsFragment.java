package com.svcp.steptracker.fragments;

import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.svcp.steptracker.R;

import java.util.HashMap;
import java.util.Map;

public class NotificationSettingsFragment extends PreferenceFragmentCompat {

    private FirebaseFirestore db;
    private String userId;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.notification_preferences, rootKey);
        
        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        // Set up daily reminder preference
        SwitchPreferenceCompat dailyReminderPref = findPreference("daily_reminder");
        if (dailyReminderPref != null) {
            dailyReminderPref.setOnPreferenceChangeListener((preference, newValue) -> {
                boolean enabled = (boolean) newValue;
                updateNotificationSetting("dailyReminder", enabled);
                return true;
            });
        }
        
        // Set up goal reminder preference
        SwitchPreferenceCompat goalReminderPref = findPreference("goal_achievement");
        if (goalReminderPref != null) {
            goalReminderPref.setOnPreferenceChangeListener((preference, newValue) -> {
                boolean enabled = (boolean) newValue;
                updateNotificationSetting("goalAchievement", enabled);
                return true;
            });
        }
        
        // Set up inactivity reminder preference
        SwitchPreferenceCompat inactivityReminderPref = findPreference("inactivity_reminder");
        if (inactivityReminderPref != null) {
            inactivityReminderPref.setOnPreferenceChangeListener((preference, newValue) -> {
                boolean enabled = (boolean) newValue;
                updateNotificationSetting("inactivityReminder", enabled);
                return true;
            });
        }
        
        // Set up reminder time preference
        Preference reminderTimePref = findPreference("reminder_time");
        if (reminderTimePref != null) {
            reminderTimePref.setOnPreferenceChangeListener((preference, newValue) -> {
                String time = newValue.toString();
                updateNotificationSetting("reminderTime", time);
                return true;
            });
        }
        
        // Load current settings from Firebase
        loadNotificationSettings();
    }
    
    /**
     * Update a notification setting in Firebase
     * 
     * @param settingKey The key of the setting to update
     * @param value The new value of the setting
     */
    private void updateNotificationSetting(String settingKey, Object value) {
        if (userId == null) return;
        
        db.collection("users").document(userId)
                .collection("settings").document("notifications")
                .update(settingKey, value)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), 
                            "Settings updated successfully", 
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), 
                            "Failed to update settings: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    
                    // If document doesn't exist, create it
                    Map<String, Object> initialSettings = new HashMap<>();
                    initialSettings.put(settingKey, value);
                    
                    db.collection("users").document(userId)
                            .collection("settings").document("notifications")
                            .set(initialSettings)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), 
                                        "Settings created successfully", 
                                        Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(ex -> {
                                Toast.makeText(getContext(), 
                                        "Failed to create settings: " + ex.getMessage(), 
                                        Toast.LENGTH_SHORT).show();
                            });
                });
    }
    
    /**
     * Load notification settings from Firebase
     */
    private void loadNotificationSettings() {
        if (userId == null) return;
        
        db.collection("users").document(userId)
                .collection("settings").document("notifications")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Update UI based on retrieved settings
                        SwitchPreferenceCompat dailyReminderPref = findPreference("daily_reminder");
                        if (dailyReminderPref != null && documentSnapshot.contains("dailyReminder")) {
                            dailyReminderPref.setChecked(documentSnapshot.getBoolean("dailyReminder"));
                        }
                        
                        SwitchPreferenceCompat goalReminderPref = findPreference("goal_achievement");
                        if (goalReminderPref != null && documentSnapshot.contains("goalAchievement")) {
                            goalReminderPref.setChecked(documentSnapshot.getBoolean("goalAchievement"));
                        }
                        
                        SwitchPreferenceCompat inactivityReminderPref = findPreference("inactivity_reminder");
                        if (inactivityReminderPref != null && documentSnapshot.contains("inactivityReminder")) {
                            inactivityReminderPref.setChecked(documentSnapshot.getBoolean("inactivityReminder"));
                        }
                        
                        Preference reminderTimePref = findPreference("reminder_time");
                        if (reminderTimePref != null && documentSnapshot.contains("reminderTime")) {
                            reminderTimePref.setSummary(documentSnapshot.getString("reminderTime"));
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), 
                            "Failed to load settings: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                });
    }
}