package com.svcp.steptracker.activities;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.svcp.steptracker.R;
import com.svcp.steptracker.utils.AuthManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";

    private TextInputLayout nameLayout, emailLayout, passwordLayout, confirmPasswordLayout;
    private TextInputEditText nameField, emailField, passwordField, confirmPasswordField;
    private MaterialButton registerButton;
    private TextView loginLink;
    private CardView formCard;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize AuthManager
        authManager = new AuthManager(this);

        // Initialize UI elements
        nameLayout = findViewById(R.id.name_layout);
        emailLayout = findViewById(R.id.email_layout);
        passwordLayout = findViewById(R.id.password_layout);
        confirmPasswordLayout = findViewById(R.id.confirm_password_layout);

        nameField = findViewById(R.id.name);
        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
        confirmPasswordField = findViewById(R.id.confirm_password);
        registerButton = findViewById(R.id.register_button);
        loginLink = findViewById(R.id.login_link);
        formCard = findViewById(R.id.form_card);

        // Add subtle animation on form card
        formCard.setAlpha(0f);
        formCard.setTranslationY(50);
        formCard.animate().alpha(1f).translationY(0).setDuration(800).setStartDelay(300).start();

        // Set click listener for register button
        registerButton.setOnClickListener(v -> registerUser());

        // Set click listener for login link
        loginLink.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });

        // Set up back button
        findViewById(R.id.back_button).setOnClickListener(v -> {
            onBackPressed();
        });

        // Clear errors on focus change
        setupFocusChangeListeners();
    }

    private void setupFocusChangeListeners() {
        View.OnFocusChangeListener clearErrorListener = (v, hasFocus) -> {
            if (hasFocus) {
                if (v == nameField) nameLayout.setError(null);
                else if (v == emailField) emailLayout.setError(null);
                else if (v == passwordField) passwordLayout.setError(null);
                else if (v == confirmPasswordField) confirmPasswordLayout.setError(null);
            }
        };

        nameField.setOnFocusChangeListener(clearErrorListener);
        emailField.setOnFocusChangeListener(clearErrorListener);
        passwordField.setOnFocusChangeListener(clearErrorListener);
        confirmPasswordField.setOnFocusChangeListener(clearErrorListener);
    }

    private void registerUser() {
        // Get input values
        String name = nameField.getText().toString().trim();
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();
        String confirmPassword = confirmPasswordField.getText().toString().trim();

        boolean isValid = true;

        // Validate input fields
        if (TextUtils.isEmpty(name)) {
            nameLayout.setError("Name is required");
            isValid = false;
        }

        if (TextUtils.isEmpty(email)) {
            emailLayout.setError("Email is required");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Please enter a valid email");
            isValid = false;
        }

        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            passwordLayout.setError("Password must be at least 6 characters");
            isValid = false;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordLayout.setError("Passwords do not match");
            isValid = false;
        }

        if (!isValid) return;

        // Show loading state
        registerButton.setEnabled(false);
        registerButton.setText("Creating account...");

        // Add loading animation - simple alpha animation
        ValueAnimator animator = ValueAnimator.ofFloat(0.6f, 1.0f);
        animator.setDuration(800);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.addUpdateListener(animation -> {
            float alpha = (float) animation.getAnimatedValue();
            registerButton.setAlpha(alpha);
        });
        animator.start();

        // Register the user
        authManager.registerWithEmail(name, email, password, isSuccess -> {
            // Stop the animation
            animator.cancel();
            registerButton.setAlpha(1.0f);
            registerButton.setEnabled(true);
            registerButton.setText("Create Account");

            if (isSuccess) {
                // Registration successful
                Toast.makeText(RegisterActivity.this,
                        "Account created successfully! Please check your email for verification.",
                        Toast.LENGTH_LONG).show();

                // Navigate to login screen with animation
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
            } else {
                // Registration failed, error is handled by AuthManager
                // Shake animation for form card on failure
                Animation shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake);
                formCard.startAnimation(shakeAnimation);
            }
        });
    }
}