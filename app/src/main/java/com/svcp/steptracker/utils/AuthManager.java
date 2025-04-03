package com.svcp.steptracker.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Class to handle all authentication operations
 */
public class AuthManager {
    private static final String TAG = "AuthManager";

    private final Context context;
    private final FirebaseAuth mAuth;
    private final FirebaseFirestore db;

    public interface AuthCallback {
        void onComplete(boolean isSuccess);
    }

    public AuthManager(Context context) {
        this.context = context;
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Register a new user with email and password
     * @param name User's name
     * @param email User's email
     * @param password User's password
     * @param callback Callback with result
     */
    public void registerWithEmail(String name, String email, String password, AuthCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Store user data in Firestore
                            storeUserProfile(user.getUid(), name, email);
                            
                            // Send email verification
                            sendEmailVerification();
                            
                            callback.onComplete(true);
                        } else {
                            Log.e(TAG, "User is null after registration");
                            Toast.makeText(context, "Registration failed, please try again",
                                    Toast.LENGTH_SHORT).show();
                            callback.onComplete(false);
                        }
                    } else {
                        Log.e(TAG, "Registration failed", task.getException());
                        Toast.makeText(context, "Registration failed: " + 
                                        task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                        callback.onComplete(false);
                    }
                });
    }

    /**
     * Login a user with email and password
     * @param email User's email
     * @param password User's password
     * @param callback Callback with result
     */
    public void loginWithEmail(String email, String password, AuthCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onComplete(true);
                    } else {
                        Log.e(TAG, "Login failed", task.getException());
                        Toast.makeText(context, "Login failed: " + 
                                        task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                        callback.onComplete(false);
                    }
                });
    }

    /**
     * Send email verification to currently signed in user
     */
    public void sendEmailVerification() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Verification email sent");
                            Toast.makeText(context, 
                                    "Verification email sent to " + user.getEmail(),
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Log.e(TAG, "Failed to send verification email", task.getException());
                            Toast.makeText(context,
                                    "Failed to send verification email",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    /**
     * Send password reset email
     * @param email User's email address
     * @param callback Callback with result
     */
    public void resetPassword(String email, AuthCallback callback) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onComplete(true);
                    } else {
                        Log.e(TAG, "Reset password failed", task.getException());
                        Toast.makeText(context, 
                                "Failed to send reset email: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                        callback.onComplete(false);
                    }
                });
    }

    /**
     * Store user profile in Firestore
     * @param userId User ID
     * @param name User's name
     * @param email User's email
     */
    private void storeUserProfile(String userId, String name, String email) {
        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("email", email);
        user.put("createdAt", java.util.Calendar.getInstance().getTime());
        user.put("dailyGoal", 10000); // Default goal of 10,000 steps

        db.collection("users")
                .document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "User profile stored successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Error storing user profile", e));
    }

    /**
     * Check if current user is logged in
     * @return true if user is logged in and email is verified
     */
    public boolean isUserLoggedIn() {
        FirebaseUser user = mAuth.getCurrentUser();
        return user != null && user.isEmailVerified();
    }

    /**
     * Sign out current user
     */
    public void signOut() {
        mAuth.signOut();
    }
    
    /**
     * Get current user
     * @return FirebaseUser object or null
     */
    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }
}