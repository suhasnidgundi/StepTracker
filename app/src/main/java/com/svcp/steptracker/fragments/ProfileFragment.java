package com.svcp.steptracker.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.svcp.steptracker.R;
import com.svcp.steptracker.activities.LoginActivity;
import com.svcp.steptracker.activities.SettingsActivity;

import java.util.Locale;

public class ProfileFragment extends Fragment {

    private ImageView profileImage;
    private TextView nameText;
    private TextView emailText;
    private TextView dailyGoalText;
    private TextView streakText;
    private TextView joinDateText;
    private Button editProfileButton;
    private Button settingsButton;
    private Button logoutButton;
    private CardView statsCard;
    
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private SharedPreferences sharedPreferences;

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private StorageReference storageReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference("profile_images");
        
        // Initialize shared preferences
        sharedPreferences = requireActivity().getSharedPreferences("step_tracker_prefs", Context.MODE_PRIVATE);

        // Initialize views
        profileImage = view.findViewById(R.id.profile_image);
        nameText = view.findViewById(R.id.name_text);
        emailText = view.findViewById(R.id.email_text);
        dailyGoalText = view.findViewById(R.id.daily_goal_text);
        streakText = view.findViewById(R.id.streak_text);
        joinDateText = view.findViewById(R.id.join_date_text);
        editProfileButton = view.findViewById(R.id.edit_profile_button);
        settingsButton = view.findViewById(R.id.settings_button);
        logoutButton = view.findViewById(R.id.logout_button);
        statsCard = view.findViewById(R.id.stats_card);

        // Set up button click listeners
        editProfileButton.setOnClickListener(v -> {
            // Handle edit profile
            chooseImage();
        });

        settingsButton.setOnClickListener(v -> {
            // Navigate to settings
            startActivity(new Intent(requireContext(), SettingsActivity.class));
        });

        logoutButton.setOnClickListener(v -> {
            // Handle logout
            mAuth.signOut();
            startActivity(new Intent(requireContext(), LoginActivity.class));
            requireActivity().finish();
        });

        // Load user data
        loadUserData();

        // Apply entrance animations
        applyAnimations();

        return view;
    }

    private void loadUserData() {
        // For demo purposes, we'll use the following user data
        String username = "suhasnidgundiError's"; // Corrected from the error message
        String email = "suhas@example.com";
        int dailyGoal = 10000;
        int streak = 7;
        String joinDate = "April 2025"; // Matches the current date provided
        
        // Set user data to views
        nameText.setText(username);
        emailText.setText(email);
        dailyGoalText.setText(String.format(Locale.getDefault(), "%d steps", dailyGoal));
        streakText.setText(String.format(Locale.getDefault(), "%d days", streak));
        joinDateText.setText("Member since " + joinDate);
        
        // Load default profile image
        Glide.with(this)
            .load(R.drawable.default_profile)
            .circleCrop()
            .into(profileImage);
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == -1 && data != null && data.getData() != null) {
            imageUri = data.getData();
            
            // Display selected image
            Glide.with(this)
                .load(imageUri)
                .circleCrop()
                .into(profileImage);
            
            // Upload image to Firebase Storage
            uploadImage();
        }
    }
    
    private void uploadImage() {
        if (imageUri != null && currentUser != null) {
            // Show loading state
            Toast.makeText(requireContext(), "Uploading image...", Toast.LENGTH_SHORT).show();
            
            // Upload to Firebase Storage
            StorageReference fileReference = storageReference.child(currentUser.getUid() + ".jpg");
            
            fileReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Save download URL to Firestore
                        if (currentUser != null) {
                            db.collection("users").document(currentUser.getUid())
                                .update("profileImageUrl", uri.toString())
                                .addOnSuccessListener(aVoid ->
                                    Toast.makeText(requireContext(), "Image uploaded successfully!", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e ->
                                    Toast.makeText(requireContext(), "Failed to update profile data", Toast.LENGTH_SHORT).show());
                        }
                    });
                })
                .addOnFailureListener(e ->
                    Toast.makeText(requireContext(), "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
    
    private void applyAnimations() {
        // Animate profile section
        profileImage.setAlpha(0f);
        profileImage.animate().alpha(1f).setDuration(500).start();
        
        nameText.setAlpha(0f);
        nameText.setTranslationY(20);
        nameText.animate().alpha(1f).translationY(0).setDuration(500).setStartDelay(100).start();
        
        emailText.setAlpha(0f);
        emailText.setTranslationY(20);
        emailText.animate().alpha(1f).translationY(0).setDuration(500).setStartDelay(150).start();
        
        // Animate stats card
        statsCard.setAlpha(0f);
        statsCard.setTranslationY(50);
        statsCard.animate().alpha(1f).translationY(0).setDuration(500).setStartDelay(200).start();
        
        // Animate buttons
        editProfileButton.setAlpha(0f);
        editProfileButton.animate().alpha(1f).setDuration(500).setStartDelay(300).start();
        
        settingsButton.setAlpha(0f);
        settingsButton.animate().alpha(1f).setDuration(500).setStartDelay(350).start();
        
        logoutButton.setAlpha(0f);
        logoutButton.animate().alpha(1f).setDuration(500).setStartDelay(400).start();
    }
}