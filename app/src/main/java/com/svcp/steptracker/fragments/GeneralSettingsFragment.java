package com.svcp.steptracker.fragments;

import android.os.Bundle;
import android.text.InputType;
import android.widget.Toast;

import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.svcp.steptracker.R;

public class GeneralSettingsFragment extends PreferenceFragmentCompat {

    private FirebaseFirestore db;
    private String userId;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.general_preferences, rootKey);
        
        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        // Set up step goal preference
        EditTextPreference stepGoalPref = findPreference("daily_step_goal");
        if (stepGoalPref != null) {
            // Set input type to number
            stepGoalPref.setOnBindEditTextListener(
                editText -> editText.setInputType(InputType.TYPE_CLASS_NUMBER)
            );
            
            // Load current step goal
            loadStepGoal();
            
            // Set up listener for changes
            stepGoalPref.setOnPreferenceChangeListener((preference, newValue) -> {
                String newGoal = (String) newValue;
                try {
                    int goal = Integer.parseInt(newGoal);
                    if (goal < 1000) {
                        Toast.makeText(getContext(), "Step goal should be at least 1000", 
                                Toast.LENGTH_SHORT).show();
                        return false;
                    } else if (goal > 100000) {
                        Toast.makeText(getContext(), "Step goal cannot exceed 100,000", 
                                Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    updateStepGoal(goal);
                    preference.setSummary(goal + " steps");
                    return true;
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Please enter a valid number", 
                            Toast.LENGTH_SHORT).show();
                    return false;
                }
            });
        }
        
        // Set up distance unit preference
        ListPreference unitPref = findPreference("distance_unit");
        if (unitPref != null) {
            unitPref.setOnPreferenceChangeListener((preference, newValue) -> {
                String unit = (String) newValue;
                updateDistanceUnit(unit);
                return true;
            });
        }
        
        // Set up auto-start tracking preference
        SwitchPreferenceCompat autoStartPref = findPreference("auto_start_tracking");
        if (autoStartPref != null) {
            autoStartPref.setOnPreferenceChangeListener((preference, newValue) -> {
                boolean enabled = (boolean) newValue;
                updateAutoStart(enabled);
                return true;
            });
        }
    }
    
    private void loadStepGoal() {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists() && documentSnapshot.contains("dailyGoal")) {
                    Long goal = documentSnapshot.getLong("dailyGoal");
                    if (goal != null) {
                        EditTextPreference stepGoalPref = findPreference("daily_step_goal");
                        if (stepGoalPref != null) {
                            stepGoalPref.setText(String.valueOf(goal));
                            stepGoalPref.setSummary(goal + " steps");
                        }
                    }
                }
            });
    }
    
    private void updateStepGoal(int goal) {
        db.collection("users").document(userId)
            .update("dailyGoal", goal)
            .addOnSuccessListener(aVoid -> 
                Toast.makeText(getContext(), "Step goal updated successfully", 
                        Toast.LENGTH_SHORT).show())
            .addOnFailureListener(e -> 
                Toast.makeText(getContext(), "Failed to update step goal", 
                        Toast.LENGTH_SHORT).show());
    }
    
    private void updateDistanceUnit(String unit) {
        db.collection("users").document(userId)
            .update("distanceUnit", unit)
            .addOnSuccessListener(aVoid -> {
                // Success, no need for a toast as the preference will update visually
            })
            .addOnFailureListener(e -> 
                Toast.makeText(getContext(), "Failed to update distance unit", 
                        Toast.LENGTH_SHORT).show());
    }
    
    private void updateAutoStart(boolean enabled) {
        db.collection("users").document(userId)
            .update("autoStartTracking", enabled)
            .addOnSuccessListener(aVoid -> {
                // Success, no need for a toast as the preference will update visually
            })
            .addOnFailureListener(e -> 
                Toast.makeText(getContext(), "Failed to update tracking settings", 
                        Toast.LENGTH_SHORT).show());
    }
}