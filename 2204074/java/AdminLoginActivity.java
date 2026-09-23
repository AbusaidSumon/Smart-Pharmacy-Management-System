package com.example.smartpharmacy;

import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AdminLoginActivity extends AppCompatActivity {

    private EditText edtAdminEmail;
    private EditText edtAdminPassword;
    private Button btnAdminLogin;
    private Button btnAdminBack;
    private TextView txtAdminForgotPassword;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        edtAdminEmail = findViewById(R.id.edtAdminEmail);
        edtAdminPassword = findViewById(R.id.edtAdminPassword);
        btnAdminLogin = findViewById(R.id.btnAdminLogin);
        btnAdminBack = findViewById(R.id.btnAdminBack);
        txtAdminForgotPassword =
                findViewById(R.id.txtAdminForgotPassword);

        // Password field
        edtAdminPassword.setInputType(
                InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_PASSWORD
        );

        btnAdminBack.setOnClickListener(v -> finish());

        btnAdminLogin.setOnClickListener(v -> loginAdmin());

        txtAdminForgotPassword.setOnClickListener(
                v -> resetPassword()
        );
    }

    private void loginAdmin() {

        String email =
                edtAdminEmail.getText()
                        .toString()
                        .trim()
                        .toLowerCase();

        String password =
                edtAdminPassword.getText()
                        .toString();

        if (email.isEmpty()) {

            edtAdminEmail.setError(
                    "Enter admin email"
            );

            edtAdminEmail.requestFocus();

            return;
        }

        if (password.isEmpty()) {

            edtAdminPassword.setError(
                    "Enter password"
            );

            edtAdminPassword.requestFocus();

            return;
        }

        // Prevent multiple clicks
        btnAdminLogin.setEnabled(false);
        btnAdminLogin.setText("Logging in...");

        // Firebase Authentication
        mAuth.signInWithEmailAndPassword(
                        email,
                        password
                )
                .addOnSuccessListener(
                        authResult -> {

                            FirebaseUser user =
                                    mAuth.getCurrentUser();

                            if (user == null) {

                                loginFailed(
                                        "Unable to verify account"
                                );

                                return;
                            }

                            String uid =
                                    user.getUid();

                            // Check all 3 admin collections
                            checkAdminAccess(
                                    uid,
                                    email
                            );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            String message =
                                    e.getMessage();

                            // Network related problem
                            if (message != null &&
                                    (
                                            message.contains(
                                                    "network"
                                            ) ||
                                                    message.contains(
                                                            "NETWORK"
                                                    ) ||
                                                    message.contains(
                                                            "timeout"
                                                    )
                                    )) {

                                loginFailed(
                                        "Network problem. Check your internet and try again."
                                );

                            } else {

                                loginFailed(
                                        "Invalid admin email or password"
                                );
                            }
                        }
                );
    }

    private void checkAdminAccess(
            String uid,
            String email
    ) {

        /*
         * Check admin1, admin2 and admin3
         * at the same time.
         *
         * This is faster than checking them
         * one after another.
         */

        Task<DocumentSnapshot> task1 =
                db.collection("admin1")
                        .document(uid)
                        .get();

        Task<DocumentSnapshot> task2 =
                db.collection("admin2")
                        .document(uid)
                        .get();

        Task<DocumentSnapshot> task3 =
                db.collection("admin3")
                        .document(uid)
                        .get();

        List<Task<DocumentSnapshot>> tasks =
                new ArrayList<>();

        tasks.add(task1);
        tasks.add(task2);
        tasks.add(task3);

        Tasks.whenAllSuccess(tasks)
                .addOnSuccessListener(
                        results -> {

                            boolean authorized = false;

                            for (Object result :
                                    results) {

                                DocumentSnapshot document =
                                        (DocumentSnapshot) result;

                                if (!document.exists()) {
                                    continue;
                                }

                                String role =
                                        document.getString(
                                                "role"
                                        );

                                String status =
                                        document.getString(
                                                "status"
                                        );

                                String savedEmail =
                                        document.getString(
                                                "email"
                                        );

                                /*
                                 * Must be:
                                 *
                                 * role = admin
                                 * status = Approved
                                 */

                                if (
                                        "admin".equalsIgnoreCase(
                                                role
                                        )
                                                &&
                                                "Approved".equalsIgnoreCase(
                                                        status
                                                )
                                ) {

                                    /*
                                     * If email exists in
                                     * Firestore, also verify it.
                                     */
                                    if (
                                            savedEmail == null
                                                    ||
                                                    savedEmail
                                                            .trim()
                                                            .equalsIgnoreCase(
                                                                    email
                                                            )
                                    ) {

                                        authorized = true;
                                        break;
                                    }
                                }
                            }

                            if (authorized) {

                                openAdminDashboard();

                            } else {

                                /*
                                 * Firebase account exists,
                                 * but this user is not an
                                 * authorized admin.
                                 */
                                mAuth.signOut();

                                loginFailed(
                                        "You are not authorized as admin"
                                );
                            }
                        }
                )
                .addOnFailureListener(
                        e -> {

                            /*
                             * Don't show "Invalid password"
                             * when Firestore/network fails.
                             */
                            loginFailed(
                                    "Unable to verify admin access. Check your internet."
                            );
                        }
                );
    }

    private void openAdminDashboard() {

        Toast.makeText(
                AdminLoginActivity.this,
                "Admin login successful",
                Toast.LENGTH_SHORT
        ).show();

        android.content.Intent intent =
                new android.content.Intent(
                        AdminLoginActivity.this,
                        AdminDashboardActivity.class
                );

        startActivity(intent);

        finish();
    }

    private void loginFailed(
            String message
    ) {

        btnAdminLogin.setEnabled(true);
        btnAdminLogin.setText(
                "Admin / Pharmacist Login"
        );

        Toast.makeText(
                AdminLoginActivity.this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }

    private void resetPassword() {

        String email =
                edtAdminEmail.getText()
                        .toString()
                        .trim()
                        .toLowerCase();

        if (email.isEmpty()) {

            edtAdminEmail.setError(
                    "Enter your admin email first"
            );

            edtAdminEmail.requestFocus();

            return;
        }

        btnAdminLogin.setEnabled(false);

        mAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(
                        unused -> {

                            btnAdminLogin.setEnabled(true);
                            btnAdminLogin.setText(
                                    "Admin / Pharmacist Login"
                            );

                            Toast.makeText(
                                    AdminLoginActivity.this,
                                    "Password reset link sent to your email",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            btnAdminLogin.setEnabled(true);
                            btnAdminLogin.setText(
                                    "Admin / Pharmacist Login"
                            );

                            Toast.makeText(
                                    AdminLoginActivity.this,
                                    "Unable to send reset email",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }
}