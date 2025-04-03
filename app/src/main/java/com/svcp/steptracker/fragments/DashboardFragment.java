package com.svcp.steptracker.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.svcp.steptracker.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class DashboardFragment extends Fragment {

    private TextView stepsCountText;
    private TextView caloriesText;
    private TextView distanceText;
    private TextView activeTimeText;
    private TextView goalText;
    private ProgressBar stepsProgressBar;
    private CardView stepsCard;
    private CardView historyCard;
    private CardView challengesCard;
    private SwipeRefreshLayout swipeRefreshLayout;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Initialize views
        stepsCountText = view.findViewById(R.id.steps_count);
        caloriesText = view.findViewById(R.id.calories_value);
        distanceText = view.findViewById(R.id.distance_value);
        activeTimeText = view.findViewById(R.id.active_time_value);
        goalText = view.findViewById(R.id.goal_value);
        stepsProgressBar = view.findViewById(R.id.steps_progress);
        stepsCard = view.findViewById(R.id.steps_card);
        historyCard = view.findViewById(R.id.history_card);
        challengesCard = view.findViewById(R.id.challenges_card);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);

        // Set up card click listeners
        setupCardClickListeners();

        // Set up swipe refresh
        swipeRefreshLayout.setOnRefreshListener(this::loadStepData);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.primary,
                R.color.accent,
                R.color.primary_dark
        );

        // Load step data
        loadStepData();

        // Apply entrance animations
        applyAnimations();

        return view;
    }

    private void setupCardClickListeners() {
        stepsCard.setOnClickListener(v -> {
            // Show detailed step information
        });

        historyCard.setOnClickListener(v -> {
            // Navigate to history fragment
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HistoryFragment())
                    .addToBackStack(null)
                    .commit();
        });

        challengesCard.setOnClickListener(v -> {
            // Show challenges or achievements
        });
    }

    private void loadStepData() {
        swipeRefreshLayout.setRefreshing(true);

        if (currentUser != null) {
            // Get today's date
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            // For demo purposes, we'll use fixed data
            // In a real app, you would fetch from a database or sensor
            int steps = 6543;
            int goal = 10000;
            float calories = 328.5f;
            float distance = 4.2f;
            int activeMinutes = 73;

            updateUI(steps, goal, calories, distance, activeMinutes);
        } else {
            // Use demo data if user is not logged in
            int steps = 6543;
            int goal = 10000;
            float calories = 328.5f;
            float distance = 4.2f;
            int activeMinutes = 73;

            updateUI(steps, goal, calories, distance, activeMinutes);
        }
    }

    private void updateUI(int steps, int goal, float calories, float distance, int activeMinutes) {
        // Update views with step data
        stepsCountText.setText(String.valueOf(steps));
        caloriesText.setText(String.format(Locale.getDefault(), "%.1f kcal", calories));
        distanceText.setText(String.format(Locale.getDefault(), "%.1f km", distance));
        activeTimeText.setText(String.format(Locale.getDefault(), "%d min", activeMinutes));
        goalText.setText(String.format(Locale.getDefault(), "%d steps", goal));

        // Update progress bar
        int progress = (int) (((float) steps / goal) * 100);
        stepsProgressBar.setProgress(progress);

        // Stop refreshing animation
        swipeRefreshLayout.setRefreshing(false);
    }

    private void applyAnimations() {
        // Animate cards
        stepsCard.setAlpha(0f);
        stepsCard.setTranslationY(50);
        stepsCard.animate().alpha(1f).translationY(0).setDuration(300).start();

        historyCard.setAlpha(0f);
        historyCard.setTranslationY(50);
        historyCard.animate().alpha(1f).translationY(0).setDuration(300).setStartDelay(100).start();

        challengesCard.setAlpha(0f);
        challengesCard.setTranslationY(50);
        challengesCard.animate().alpha(1f).translationY(0).setDuration(300).setStartDelay(200).start();
    }
}