package com.svcp.steptracker.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.svcp.steptracker.R;
import com.svcp.steptracker.models.StepHistoryItem;

import java.util.List;
import java.util.Locale;

public class StepHistoryAdapter extends RecyclerView.Adapter<StepHistoryAdapter.ViewHolder> {

    private List<StepHistoryItem> historyItems;
    private static final int GOAL = 10000; // Default goal

    public StepHistoryAdapter(List<StepHistoryItem> historyItems) {
        this.historyItems = historyItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_step_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StepHistoryItem item = historyItems.get(position);
        
        holder.dateText.setText(item.getDate());
        holder.stepsText.setText(String.format(Locale.getDefault(), "%d steps", item.getSteps()));
        holder.distanceText.setText(String.format(Locale.getDefault(), "%.1f km", item.getDistance()));
        holder.caloriesText.setText(String.format(Locale.getDefault(), "%.1f kcal", item.getCalories()));
        
        // Calculate progress percentage
        int progress = (int) (((float) item.getSteps() / GOAL) * 100);
        holder.progressBar.setProgress(progress);
    }

    @Override
    public int getItemCount() {
        return historyItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dateText;
        TextView stepsText;
        TextView distanceText;
        TextView caloriesText;
        ProgressBar progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.date_text);
            stepsText = itemView.findViewById(R.id.steps_text);
            distanceText = itemView.findViewById(R.id.distance_text);
            caloriesText = itemView.findViewById(R.id.calories_text);
            progressBar = itemView.findViewById(R.id.progress_bar);
        }
    }
}