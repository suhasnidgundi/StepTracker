package com.svcp.steptracker.activities;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.svcp.steptracker.R;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputLayout emailLayout;
    private TextInputEditText emailField;
    private MaterialButton resetButton;
    private TextView backToLoginLink;
    private CardView formCard;
    private FirebaseAuth mAuth;
    private ValueAnimator animator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI elements
        emailLayout = findViewById(R.id.email_layout);
        emailField = findViewById(R.id.email);
        resetButton = findViewById(R.id.reset_button);
        formCard = findViewById(R.id.form_card);
        backToLoginLink = findViewById(R.id.back_to_login_link);

        // Add entrance animations
        formCard.setAlpha(0f);
        formCard.setTranslationY(50);
        formCard.animate().alpha(1f).translationY(0).setDuration(800).setStartDelay(300).start();

        resetButton.setAlpha(0f);
        resetButton.animate().alpha(1f).setDuration(800).setStartDelay(500).start();

        // Set click listener for reset button
        resetButton.setOnClickListener(v -> resetPassword());

        // Set up back button
        findViewById(R.id.back_button).setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        // Set up back to login link
        backToLoginLink.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        // Setup focus change listener to clear errors
        emailField.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                emailLayout.setError(null);
            }
        });
    }

    private void resetPassword() {
        String email = emailField.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailLayout.setError("Email is required");
            // Shake the form to indicate error
            Animation shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake);
            formCard.startAnimation(shakeAnimation);
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Please enter a valid email");
            // Shake the form to indicate error
            Animation shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake);
            formCard.startAnimation(shakeAnimation);
            return;
        }

        // Show loading state
        resetButton.setEnabled(false);
        resetButton.setText("Sending...");

        // Add loading animation
        animator = ValueAnimator.ofFloat(0.6f, 1.0f);
        animator.setDuration(800);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.addUpdateListener(animation -> {
            float alpha = (float) animation.getAnimatedValue();
            resetButton.setAlpha(alpha);
        });
        animator.start();

        // Send password reset email
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    // Stop animation
                    if (animator != null) {
                        animator.cancel();
                    }
                    resetButton.setAlpha(1.0f);
                    resetButton.setEnabled(true);
                    resetButton.setText("Send Reset Link");

                    if (task.isSuccessful()) {
                        // Success animation - pulse the form card
                        formCard.animate()
                                .scaleX(1.05f)
                                .scaleY(1.05f)
                                .setDuration(200)
                                .withEndAction(() ->
                                        formCard.animate()
                                                .scaleX(1.0f)
                                                .scaleY(1.0f)
                                                .setDuration(200)
                                                .start())
                                .start();

                        Toast.makeText(ForgotPasswordActivity.this,
                                "Password reset email sent. Check your inbox.",
                                Toast.LENGTH_LONG).show();

                        // Allow a moment for the user to see the success message
                        formCard.postDelayed(() -> {
                            finish();
                            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                        }, 1500);
                    } else {
                        // Error animation
                        Animation shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake);
                        formCard.startAnimation(shakeAnimation);

                        Toast.makeText(ForgotPasswordActivity.this,
                                "Failed to send reset email: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (animator != null) {
            animator.cancel();
        }
    }
}