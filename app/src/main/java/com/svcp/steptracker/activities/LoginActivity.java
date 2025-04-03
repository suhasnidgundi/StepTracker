package com.svcp.steptracker.activities;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.svcp.steptracker.R;
import com.svcp.steptracker.utils.AuthManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 9001;

    private TextInputLayout emailLayout, passwordLayout;
    private TextInputEditText emailField, passwordField;
    private MaterialButton loginButton;
    private TextView registerLink, forgotPasswordLink;
    private SignInButton googleSignInButton;
    private MaterialCardView googleSignInContainer;
    private CardView formCard;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private AuthManager authManager;
    private FirebaseFirestore db;
    private ValueAnimator animator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize AuthManager
        authManager = new AuthManager(this);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize UI elements
        emailLayout = findViewById(R.id.email_layout);
        passwordLayout = findViewById(R.id.password_layout);
        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
        loginButton = findViewById(R.id.login_button);
        registerLink = findViewById(R.id.register_link);
        forgotPasswordLink = findViewById(R.id.forgot_password_link);
        googleSignInButton = findViewById(R.id.google_sign_in_button);
        googleSignInContainer = findViewById(R.id.google_sign_in_container);
        formCard = findViewById(R.id.form_card);

        // Add entrance animations
        formCard.setAlpha(0f);
        formCard.setTranslationY(50);
        formCard.animate().alpha(1f).translationY(0).setDuration(800).setStartDelay(300).start();

        loginButton.setAlpha(0f);
        loginButton.animate().alpha(1f).setDuration(800).setStartDelay(500).start();

        // Set click listeners
        loginButton.setOnClickListener(v -> loginUser());

        registerLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        forgotPasswordLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        googleSignInContainer.setOnClickListener(v -> signInWithGoogle());

        // Setup focus change listeners to clear errors
        setupFocusChangeListeners();

        // Check if user is already signed in
        if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().isEmailVerified()) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }

    private void setupFocusChangeListeners() {
        View.OnFocusChangeListener clearErrorListener = (v, hasFocus) -> {
            if (hasFocus) {
                if (v == emailField) emailLayout.setError(null);
                else if (v == passwordField) passwordLayout.setError(null);
            }
        };

        emailField.setOnFocusChangeListener(clearErrorListener);
        passwordField.setOnFocusChangeListener(clearErrorListener);
    }

    private void loginUser() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        boolean isValid = true;

        if (TextUtils.isEmpty(email)) {
            emailLayout.setError("Email is required");
            isValid = false;
        }

        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError("Password is required");
            isValid = false;
        }

        if (!isValid) return;

        // Show progress bar or disable button to indicate loading
        loginButton.setEnabled(false);
        loginButton.setText("Signing in...");

        // Add loading animation
        animator = ValueAnimator.ofFloat(0.6f, 1.0f);
        animator.setDuration(800);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.addUpdateListener(animation -> {
            float alpha = (float) animation.getAnimatedValue();
            loginButton.setAlpha(alpha);
        });
        animator.start();

        authManager.loginWithEmail(email, password, isSuccess -> {
            // Stop animation
            if (animator != null) {
                animator.cancel();
            }
            loginButton.setAlpha(1.0f);
            loginButton.setEnabled(true);
            loginButton.setText("Sign In");

            if (isSuccess) {
                // Check if email is verified
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null && user.isEmailVerified()) {
                    Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this,
                            "Please verify your email before logging in",
                            Toast.LENGTH_LONG).show();
                    // Send verification email again
                    authManager.sendEmailVerification();
                    mAuth.signOut();

                    // Show error animation
                    Animation shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake);
                    formCard.startAnimation(shakeAnimation);
                }
            } else {
                // Login failed, error is handled by AuthManager
                // Show error animation
                Animation shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake);
                formCard.startAnimation(shakeAnimation);
            }
        });
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed
                Log.w(TAG, "Google sign in failed", e);
                Toast.makeText(this, "Google sign in failed: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        // Show loading state
        googleSignInContainer.setEnabled(false);

        // Show progress with animation
        ValueAnimator googleAnimator = ValueAnimator.ofFloat(1.0f, 0.7f);
        googleAnimator.setDuration(400);
        googleAnimator.setRepeatCount(ValueAnimator.INFINITE);
        googleAnimator.setRepeatMode(ValueAnimator.REVERSE);
        googleAnimator.addUpdateListener(animation -> {
            float alpha = (float) animation.getAnimatedValue();
            googleSignInContainer.setAlpha(alpha);
        });
        googleAnimator.start();

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    // Stop animation
                    googleAnimator.cancel();
                    googleSignInContainer.setAlpha(1.0f);
                    googleSignInContainer.setEnabled(true);

                    if (task.isSuccessful()) {
                        // Sign in success
                        FirebaseUser user = mAuth.getCurrentUser();

                        // Check if this is a new user
                        boolean isNewUser = task.getResult().getAdditionalUserInfo().isNewUser();

                        if (isNewUser) {
                            // Create a new user document in Firestore
                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("name", user.getDisplayName());
                            userMap.put("email", user.getEmail());
                            userMap.put("createdAt", java.util.Calendar.getInstance().getTime());
                            userMap.put("dailyGoal", 10000); // Default goal

                            db.collection("users")
                                    .document(user.getUid())
                                    .set(userMap)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(LoginActivity.this,
                                                "Account created successfully",
                                                Toast.LENGTH_SHORT).show();
                                        goToMainActivity();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.w(TAG, "Error adding user document", e);
                                        Toast.makeText(LoginActivity.this,
                                                "Account created, but profile setup failed. " +
                                                        "Please try again later.",
                                                Toast.LENGTH_LONG).show();
                                        goToMainActivity();
                                    });
                        } else {
                            goToMainActivity();
                        }
                    } else {
                        // Sign in failed
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(LoginActivity.this,
                                "Authentication failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();

                        // Show error animation
                        Animation shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake);
                        googleSignInContainer.startAnimation(shakeAnimation);
                    }
                });
    }

    private void goToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (animator != null) {
            animator.cancel();
        }
    }
}