package com.svcp.steptracker.services;


import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.svcp.steptracker.utils.FirebaseUtils;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StepCounterService extends Service implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor stepSensor;
    private int stepCount = 0;
    private String currentDate;
    private FirebaseUtils firebaseUtils;

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        firebaseUtils = new FirebaseUtils(this);

        // Get today's date in yyyy-MM-dd format
        currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Load the previous step count from Firebase
        loadStepCountFromFirebase();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Register the step counter sensor listener
        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        return START_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            // Get the step count from the sensor
            int totalSteps = (int) event.values[0];

            // Update the step count in Firebase
            updateStepCount(totalSteps);

            // Broadcast the step count to update the UI
            Intent intent = new Intent("step-counter-update");
            intent.putExtra("stepCount", stepCount);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    private void loadStepCountFromFirebase() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        firebaseUtils.getDailySteps(userId, currentDate, result -> {
            if (result != null) {
                stepCount = result.intValue();
                // Broadcast initial step count
                Intent intent = new Intent("step-counter-update");
                intent.putExtra("stepCount", stepCount);
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            }
        });
    }

    private void updateStepCount(int totalSteps) {
        // Update the step count for today
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        firebaseUtils.updateDailySteps(userId, currentDate, totalSteps);
        stepCount = totalSteps;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed for this implementation
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unregister the sensor listener
        sensorManager.unregisterListener(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}