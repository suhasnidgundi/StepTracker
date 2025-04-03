package com.svcp.steptracker.utils;

import android.content.Context;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class FirebaseUtils {

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public FirebaseUtils(Context context) {
        // Ensure Firebase is initialized
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context);
        }

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public void updateDailySteps(String userId, String date, int stepCount) {
        Map<String, Object> steps = new HashMap<>();
        steps.put("count", stepCount);
        steps.put("date", date);
        steps.put("userId", userId);

        db.collection("steps")
                .document(userId + "_" + date)
                .set(steps)
                .addOnSuccessListener(aVoid -> {
                    // Success
                })
                .addOnFailureListener(e -> {
                    // Handle error
                });
    }

    public void getDailySteps(String userId, String date, Consumer<Long> callback) {
        db.collection("steps")
                .document(userId + "_" + date)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.get("count") != null) {
                        Long count = documentSnapshot.getLong("count");
                        callback.accept(count);
                    } else {
                        callback.accept(0L);
                    }
                })
                .addOnFailureListener(e -> callback.accept(0L));
    }

    public void getStepHistory(String userId, int days, Consumer<Map<String, Long>> callback) {
        db.collection("steps")
                .whereEqualTo("userId", userId)
                .limit(days)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, Long> history = new HashMap<>();
                    queryDocumentSnapshots.forEach(document -> {
                        String date = document.getString("date");
                        Long count = document.getLong("count");
                        if (date != null && count != null) {
                            history.put(date, count);
                        }
                    });
                    callback.accept(history);
                })
                .addOnFailureListener(e -> callback.accept(new HashMap<>()));
    }
}