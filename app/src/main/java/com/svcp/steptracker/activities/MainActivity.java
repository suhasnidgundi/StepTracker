package com.svcp.steptracker.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.svcp.steptracker.R;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.svcp.steptracker.adapters.StepHistoryAdapter;
import com.svcp.steptracker.models.DailySteps;
import com.svcp.steptracker.services.StepCounterService;
import com.svcp.steptracker.utils.FirebaseUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int DAILY_GOAL = 10000;

    private TextView stepCountText;
    private TextView goalText;
    private LinearProgressIndicator goalProgress;
    private RecyclerView historyRecycler;
    private StepHistoryAdapter historyAdapter;
    private FirebaseUtils firebaseUtils;
    private int currentStepCount = 0;

    private BroadcastReceiver stepUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            currentStepCount = intent.getIntExtra("stepCount", 0);
            updateUI();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase utils
        firebaseUtils = new FirebaseUtils(this);

        // Check if user is logged in
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Set up toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize UI elements
        stepCountText = findViewById(R.id.step_count);
        goalText = findViewById(R.id.goal_text);
        goalProgress = findViewById(R.id.goal_progress);
        historyRecycler = findViewById(R.id.history_recycler);

        // Set up RecyclerView for history
        historyRecycler.setLayoutManager(new LinearLayoutManager(this));
        historyAdapter = new StepHistoryAdapter(new ArrayList<>());
        historyRecycler.setAdapter(historyAdapter);

        // Set up progress bar
        goalProgress.setMax(DAILY_GOAL);

        // Start step counter service
        startService(new Intent(this, StepCounterService.class));

        // Register broadcast receiver for step updates
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(stepUpdateReceiver, new IntentFilter("step-counter-update"));

        // Load step history
        loadStepHistory();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update UI with current step count
        updateUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister broadcast receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(stepUpdateReceiver);
    }

    private void updateUI() {
        stepCountText.setText(String.valueOf(currentStepCount));
        int progressPercentage = (int) (((float) currentStepCount / DAILY_GOAL) * 100);
        goalProgress.setProgress(currentStepCount);
        goalText.setText(progressPercentage + "% of daily goal (" + DAILY_GOAL + " steps)");
    }

    private void loadStepHistory() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        firebaseUtils.getStepHistory(userId, 7, stepsMap -> {
            List<DailySteps> stepsList = new ArrayList<>();
            for (Map.Entry<String, Long> entry : stepsMap.entrySet()) {
                stepsList.add(new DailySteps(entry.getKey(), entry.getValue().intValue()));
            }
            historyAdapter.updateData(stepsList);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return true;
        }

        if (item.getItemId() == R.id.menu_settings) {
            // Open settings activity
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}