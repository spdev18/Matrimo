package com.techotd.matrimo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class HelpFeedbackActivity extends AppCompatActivity {

    private static final String TAG = "HelpFeedbackActivity";
    private TextInputEditText feedbackInput;
    private TextInputLayout feedbackInputLayout;
    private android.widget.TextView emailText;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_feedback);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        emailText = findViewById(R.id.emailText);
        feedbackInput = findViewById(R.id.feedbackInput);
        feedbackInputLayout = findViewById(R.id.feedbackInputLayout);
        MaterialButton submitButton = findViewById(R.id.submitButton);

        // Back button to finish the activity
        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        // Display user's email
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.getEmail() != null) {
            emailText.setText(currentUser.getEmail());
            Log.d(TAG, "User email: " + currentUser.getEmail());
        } else {
            emailText.setText("Not signed in");
            submitButton.setEnabled(false); // Disable submit if not signed in
            Log.w(TAG, "No user signed in");
        }

        // Submit button to send feedback via email
        submitButton.setOnClickListener(v -> {
            String feedback = feedbackInput.getText() != null ? feedbackInput.getText().toString().trim() : "";
            if (feedback.isEmpty()) {
                feedbackInputLayout.setError("Feedback cannot be empty");
                return;
            }
            feedbackInputLayout.setError(null); // Clear error
            sendFeedbackEmail(feedback);
        });
    }

    private void sendFeedbackEmail(String feedback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null || currentUser.getEmail() == null) {
            Toast.makeText(this, "Please sign in to send feedback", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "User not signed in during feedback submission");
            return;
        }

        // Format the current date and time (01:56 PM IST, May 15, 2025)
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a 'IST'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
        String timestamp = sdf.format(new Date(1747377360000L)); // Timestamp for May 15, 2025, 01:56 PM IST

        // Create email intent
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822"); // Email MIME type
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"dev.aruntiwari@gmail.com"}); // Support email
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "VoxaAI Feedback");
        emailIntent.putExtra(Intent.EXTRA_TEXT,
                "Feedback from: " + currentUser.getEmail() + "\n" +
                        "Timestamp: " + timestamp + "\n\n" +
                        feedback);

        try {
            startActivity(Intent.createChooser(emailIntent, "Send feedback via"));
            Toast.makeText(this, "Opening email client...", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Email intent launched successfully");
        } catch (Exception e) {
            Toast.makeText(this, "No email client found. Please install one.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Failed to launch email intent: " + e.getMessage());
        }
    }
}