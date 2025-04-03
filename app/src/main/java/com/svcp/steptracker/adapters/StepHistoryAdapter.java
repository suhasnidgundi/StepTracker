package com.svcp.steptracker.adapters;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.svcp.steptracker.R;
import com.svcp.steptracker.models.DailySteps;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StepHistoryAdapter extends RecyclerView.Adapter<StepHistoryAdapter.ViewHolder> {

    private static final int DAILY_GOAL = 10000;
    private List<DailySteps> stepsList;

    public StepHistoryAdapter(List<DailySteps> stepsList) {
        this.stepsList = stepsList;
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
        DailySteps dailySteps = stepsList.get(position);

        // Format date for display
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dailySteps.getDate());
            holder.dateText.setText(outputFormat.format(date));
        } catch (ParseException e) {
            holder.dateText.setText(dailySteps.getDate());
        }

        holder.stepsText.setText(String.valueOf(dailySteps.getStepCount()) + " steps");

        // Update progress bar
        holder.progressBar.setMax(DAILY_GOAL);
        holder.progressBar.setProgress(dailySteps.getStepCount());
    }

    @Override
    public int getItemCount() {
        return stepsList.size();
    }

    public void updateData(List<DailySteps> newData) {
        this.stepsList = newData;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dateText;
        TextView stepsText;
        LinearProgressIndicator progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.date_text);
            stepsText = itemView.findViewById(R.id.steps_text);
            progressBar = itemView.findViewById(R.id.progress_bar);
        }
    }
}