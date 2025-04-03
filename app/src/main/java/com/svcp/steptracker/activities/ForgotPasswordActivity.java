package com.svcp.steptracker.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.svcp.steptracker.R;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputEditText emailField;
    private MaterialButton resetButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI elements
        emailField = findViewById(R.id.email);
        resetButton = findViewById(R.id.reset_button);

        // Set click listener for reset button
        resetButton.setOnClickListener(v -> resetPassword());

        // Set up back button in toolbar
        findViewById(R.id.back_button).setOnClickListener(v -> finish());
    }

    private void resetPassword() {
        String email = emailField.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailField.setError("Email is required");
            return;
        }

        // Show loading state
        resetButton.setEnabled(false);
        resetButton.setText("Sending...");

        // Send password reset email
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    resetButton.setEnabled(true);
                    resetButton.setText("Reset Password");

                    if (task.isSuccessful()) {
                        Toast.makeText(ForgotPasswordActivity.this,
                                "Password reset email sent. Check your inbox.",
                                Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(ForgotPasswordActivity.this,
                                "Failed to send reset email: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}