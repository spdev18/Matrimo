package com.techotd.matrimo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class SettingsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextView emailText;
    private FirebaseFirestore db;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        emailText = findViewById(R.id.emailText);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            emailText.setText(currentUser.getEmail());
        } else {
            emailText.setText("Not signed in");
            findViewById(R.id.signOut).setEnabled(false);
            findViewById(R.id.deleteAccount).setEnabled(false);
        }

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        findViewById(R.id.signOut).setOnClickListener(v -> signOut());
        findViewById(R.id.deleteAccount).setOnClickListener(v -> deleteAccount());
    }

    // Removed all membership/premium related methods and fields

    private void signOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sign Out");
        builder.setMessage("Are you sure you want to sign out?");
        builder.setPositiveButton("Sign Out", (dialog, which) -> {
            // Sign out from Firebase
            mAuth.signOut();

            // Sign out from Google Sign-In
            mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void deleteAccount() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "No user signed in", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Account");
        builder.setMessage("Note: If you delete your account, your transcriptions will also be deleted. This action cannot be undone. \nAre you sure you want to delete your account?");
        builder.setPositiveButton("Delete", (dialog, which) -> {
            // Delete Firestore data
            db.collection("users").document(user.getUid()).collection("transcripts")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (var doc : queryDocumentSnapshots) {
                            doc.getReference().delete();
                        }
                        db.collection("users").document(user.getUid())
                                .delete()
                                .addOnSuccessListener(aVoid -> deleteAuthAccount(user))
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to delete user data", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to delete transcripts", Toast.LENGTH_SHORT).show();
                    });
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void deleteAuthAccount(FirebaseUser user) {
        user.delete()
                .addOnSuccessListener(aVoid -> {
                    mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
                        Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    });
                })
                .addOnFailureListener(e -> {
                    if (e.getMessage().contains("requires-recent-login")) {
                        Toast.makeText(this, "Please sign in again to delete account", Toast.LENGTH_LONG).show();
                        signOut();
                    } else {
                        Toast.makeText(this, "Failed to delete account: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}