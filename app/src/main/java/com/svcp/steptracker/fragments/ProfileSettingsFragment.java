package com.svcp.steptracker.fragments;

import android.os.Bundle;
import android.text.InputType;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.svcp.steptracker.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileSettingsFragment extends PreferenceFragmentCompat {

    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.profile_preferences, rootKey);
        
        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        
        if (currentUser == null) {
            Toast.makeText(getContext(), "Error loading profile. Please login again.", 
                    Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Set up display name preference
        EditTextPreference displayNamePref = findPreference("display_name");
        if (displayNamePref != null) {
            // Set current display name
            displayNamePref.setText(currentUser.getDisplayName());
            displayNamePref.setSummary(currentUser.getDisplayName());
            
            // Set up listener for changes
            displayNamePref.setOnPreferenceChangeListener((preference, newValue) -> {
                String newName = (String) newValue;
                updateDisplayName(newName);
                preference.setSummary(newName);
                return true;
            });
        }
        
        // Set up email preference (read-only)
        Preference emailPref = findPreference("email");
        if (emailPref != null) {
            emailPref.setSummary(currentUser.getEmail());
        }
        
        // Set up height preference
        EditTextPreference heightPref = findPreference("user_height");
        if (heightPref != null) {
            // Set input type to number
            heightPref.setOnBindEditTextListener(
                editText -> editText.setInputType(InputType.TYPE_CLASS_NUMBER)
            );
            
            // Load height from Firestore
            loadUserMetrics();
            
            // Set up listener for changes
            heightPref.setOnPreferenceChangeListener((preference, newValue) -> {
                String newHeight = (String) newValue;
                updateUserMetric("height", newHeight);
                preference.setSummary(newHeight + " cm");
                return true;
            });
        }
        
        // Set up weight preference
        EditTextPreference weightPref = findPreference("user_weight");
        if (weightPref != null) {
            // Set input type to number with decimals
            weightPref.setOnBindEditTextListener(
                editText -> editText.setInputType(
                    InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL)
            );
            
            // Set up listener for changes
            weightPref.setOnPreferenceChangeListener((preference, newValue) -> {
                String newWeight = (String) newValue;
                updateUserMetric("weight", newWeight);
                preference.setSummary(newWeight + " kg");
                return true;
            });
        }
    }
    
    private void loadUserMetrics() {
        db.collection("users").document(currentUser.getUid())
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    // Get height
                    String height = documentSnapshot.getString("height");
                    EditTextPreference heightPref = findPreference("user_height");
                    if (heightPref != null && height != null) {
                        heightPref.setText(height);
                        heightPref.setSummary(height + " cm");
                    }
                    
                    // Get weight
                    String weight = documentSnapshot.getString("weight");
                    EditTextPreference weightPref = findPreference("user_weight");
                    if (weightPref != null && weight != null) {
                        weightPref.setText(weight);
                        weightPref.setSummary(weight + " kg");
                    }
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Failed to load profile data", 
                        Toast.LENGTH_SHORT).show();
            });
    }
    
    private void updateDisplayName(String newName) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .build();
        
        currentUser.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Also update in Firestore
                        db.collection("users").document(currentUser.getUid())
                            .update("name", newName)
                            .addOnSuccessListener(aVoid -> 
                                Toast.makeText(getContext(), "Profile updated successfully", 
                                        Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> 
                                Toast.makeText(getContext(), "Failed to update profile", 
                                        Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(getContext(), "Failed to update profile", 
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    private void updateUserMetric(String field, String value) {
        db.collection("users").document(currentUser.getUid())
            .update(field, value)
            .addOnSuccessListener(aVoid -> 
                Toast.makeText(getContext(), "Updated successfully", 
                        Toast.LENGTH_SHORT).show())
            .addOnFailureListener(e -> 
                Toast.makeText(getContext(), "Failed to update " + field, 
                        Toast.LENGTH_SHORT).show());
    }
}