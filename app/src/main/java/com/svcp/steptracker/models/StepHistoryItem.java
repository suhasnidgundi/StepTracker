package com.svcp.steptracker.models;

public class StepHistoryItem {
    private String date;
    private int steps;
    private float distance;
    private float calories;

    public StepHistoryItem(String date, int steps, float distance, float calories) {
        this.date = date;
        this.steps = steps;
        this.distance = distance;
        this.calories = calories;
    }

    public String getDate() {
        return date;
    }

    public int getSteps() {
        return steps;
    }

    public float getDistance() {
        return distance;
    }

    public float getCalories() {
        return calories;
    }
}