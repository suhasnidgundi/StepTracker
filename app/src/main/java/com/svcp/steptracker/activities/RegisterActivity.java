package com.svcp.steptracker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.svcp.steptracker.R;
import com.svcp.steptracker.utils.AuthManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";

    private TextInputEditText nameField, emailField, passwordField, confirmPasswordField;
    private MaterialButton registerButton;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize AuthManager
        authManager = new AuthManager(this);

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

        // Register the user
        authManager.registerWithEmail(name, email, password, isSuccess -> {
            registerButton.setEnabled(true);
            registerButton.setText("Register");
            
            if (isSuccess) {
                // Registration successful
                Toast.makeText(RegisterActivity.this,
                        "Account created successfully! Please check your email for verification.",
                        Toast.LENGTH_LONG).show();
                
                // Return to login screen
                finish();
            } else {
                // Registration failed, error is handled by AuthManager
            }
        });
    }
}