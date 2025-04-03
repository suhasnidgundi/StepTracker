package com.svcp.steptracker.models;

public class DailySteps {
    private String date;
    private int stepCount;

    public DailySteps(String date, int stepCount) {
        this.date = date;
        this.stepCount = stepCount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getStepCount() {
        return stepCount;
    }

    public void setStepCount(int stepCount) {
        this.stepCount = stepCount;
    }
}