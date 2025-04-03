package com.svcp.steptracker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.svcp.steptracker.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText nameField, emailField, passwordField, confirmPasswordField;
    private MaterialButton registerButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI elements
        nameField = findViewById(R.id.name);
        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
        confirmPasswordField = findViewById(R.id.confirm_password);
        registerButton = findViewById(R.id.register_button);

        // Set click listener for register button
        registerButton.setOnClickListener(v -> registerUser());

        // Set up back button in toolbar
        findViewById(R.id.back_button).setOnClickListener(v -> finish());
    }

    private void registerUser() {
        // Get input values
        String name = nameField.getText().toString().trim();
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();
        String confirmPassword = confirmPasswordField.getText().toString().trim();

        // Validate input fields
        if (TextUtils.isEmpty(name)) {
            nameField.setError("Name is required");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            emailField.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordField.setError("Password is required");
            return;
        }

        if (password.length() < 6) {
            passwordField.setError("Password must be at least 6 characters");
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordField.setError("Passwords do not match");
            return;
        }

        // Show loading state
        registerButton.setEnabled(false);
        registerButton.setText("Creating account...");

        // Create user with email and password
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Registration successful
                        String userId = mAuth.getCurrentUser().getUid();

                        // Store user profile information in Firestore
                        Map<String, Object> user = new HashMap<>();
                        user.put("name", name);
                        user.put("email", email);
                        user.put("createdAt", java.util.Calendar.getInstance().getTime());
                        user.put("dailyGoal", 10000); // Default goal of 10,000 steps

                        db.collection("users")
                                .document(userId)
                                .set(user)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(RegisterActivity.this,
                                            "Account created successfully", Toast.LENGTH_SHORT).show();

                                    // Navigate to main activity
                                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                            Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    // If storing user data fails, still allow login but show warning
                                    Toast.makeText(RegisterActivity.this,
                                            "Account created, but profile setup failed. Please try again later.",
                                            Toast.LENGTH_LONG).show();

                                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                });
                    } else {
                        // If registration fails, display error message
                        registerButton.setEnabled(true);
                        registerButton.setText("Register");
                        String errorMessage = task.getException().getMessage();
                        Toast.makeText(RegisterActivity.this,
                                "Registration failed: " + errorMessage,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}