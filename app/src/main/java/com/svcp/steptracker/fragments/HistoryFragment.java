package com.svcp.steptracker.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.svcp.steptracker.R;
import com.svcp.steptracker.adapters.StepHistoryAdapter;
import com.svcp.steptracker.models.StepHistoryItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class HistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private StepHistoryAdapter adapter;
    private List<StepHistoryItem> historyItems;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView emptyStateText;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Initialize views
        recyclerView = view.findViewById(R.id.history_recycler_view);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        emptyStateText = view.findViewById(R.id.empty_state_text);

        // Set up recycler view
        historyItems = new ArrayList<>();
        adapter = new StepHistoryAdapter(historyItems);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Set up swipe refresh
        swipeRefreshLayout.setOnRefreshListener(this::loadHistoryData);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.primary,
                R.color.accent,
                R.color.primary_dark
        );

        // Load history data
        loadHistoryData();

        return view;
    }

    private void loadHistoryData() {
        swipeRefreshLayout.setRefreshing(true);
        
        // For demo purposes, we'll use sample data
        // In a real app, you would fetch from a database
        historyItems.clear();
        
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        Random random = new Random();
        
        // Generate data for the past 14 days
        for (int i = 0; i < 14; i++) {
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            Date date = calendar.getTime();
            String dateString = dateFormat.format(date);
            
            // Generate random data
            int steps = 2000 + random.nextInt(10000);
            float distance = steps * 0.0008f;  // Rough estimate
            float calories = steps * 0.05f;     // Rough estimate
            
            StepHistoryItem item = new StepHistoryItem(dateString, steps, distance, calories);
            historyItems.add(item);
        }
        
        // Show empty state if no data
        if (historyItems.isEmpty()) {
            emptyStateText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyStateText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
        
        // Update the adapter
        adapter.notifyDataSetChanged();
        swipeRefreshLayout.setRefreshing(false);
    }
}