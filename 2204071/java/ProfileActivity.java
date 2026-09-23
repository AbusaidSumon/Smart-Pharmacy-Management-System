package com.example.smartpharmacy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private TextView txtName;
    private TextView txtEmail;
    private TextView txtPhone;
    private TextView txtRole;

    private Button btnBack;
    private Button btnChangePassword;
    private Button btnLogout;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Find Views
        txtName = findViewById(R.id.txtName);
        txtEmail = findViewById(R.id.txtEmail);
        txtPhone = findViewById(R.id.txtPhone);
        txtRole = findViewById(R.id.txtRole);

        btnBack = findViewById(R.id.btnBack);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnLogout = findViewById(R.id.btnLogout);

        // Load user profile
        loadProfile();

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Change Password
        btnChangePassword.setOnClickListener(v -> changePassword());

        // Logout
        btnLogout.setOnClickListener(v -> {

            // Firebase থেকে logout
            auth.signOut();

            Toast.makeText(
                    ProfileActivity.this,
                    "Logged out successfully",
                    Toast.LENGTH_SHORT
            ).show();

            // MainActivity / Home Page এ নিয়ে যাবে
            Intent intent = new Intent(
                    ProfileActivity.this,
                    MainActivity.class
            );

            // আগের Dashboard/Profile stack clear করবে
            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);
            finish();
        });
    }

    // =========================
    // Load Profile
    // =========================

    private void loadProfile() {

        if (auth.getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "User not logged in",
                    Toast.LENGTH_SHORT
            ).show();

            goToMainActivity();
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        // Email Firebase Authentication থেকে
        String email = auth.getCurrentUser().getEmail();

        if (email != null) {
            txtEmail.setText(email);
        } else {
            txtEmail.setText("Not available");
        }

        // Firestore থেকে User information
        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (documentSnapshot.exists()) {

                        String name =
                                documentSnapshot.getString("name");

                        String phone =
                                documentSnapshot.getString("phone");

                        String role =
                                documentSnapshot.getString("role");

                        // Name
                        if (name != null && !name.isEmpty()) {
                            txtName.setText(name);
                        } else {
                            txtName.setText("Not available");
                        }

                        // Phone
                        if (phone != null && !phone.isEmpty()) {
                            txtPhone.setText(phone);
                        } else {
                            txtPhone.setText("Not available");
                        }

                        // Role
                        if (role != null && !role.isEmpty()) {
                            txtRole.setText(role);
                        } else {
                            txtRole.setText("User");
                        }

                    } else {

                        txtName.setText("Not available");
                        txtPhone.setText("Not available");
                        txtRole.setText("User");

                        Toast.makeText(
                                ProfileActivity.this,
                                "Profile information not found",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            ProfileActivity.this,
                            "Failed to load profile",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    // =========================
    // Change Password
    // =========================

    private void changePassword() {

        if (auth.getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "User not logged in",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String email = auth.getCurrentUser().getEmail();

        if (email == null || email.isEmpty()) {

            Toast.makeText(
                    this,
                    "Email not found",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused -> {

                    Toast.makeText(
                            ProfileActivity.this,
                            "Password reset link sent to your email",
                            Toast.LENGTH_LONG
                    ).show();
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            ProfileActivity.this,
                            "Failed to send password reset email",
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    // =========================
    // Go To Main Activity
    // =========================

    private void goToMainActivity() {

        Intent intent = new Intent(
                ProfileActivity.this,
                MainActivity.class
        );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }
}