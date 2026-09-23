package com.example.smartpharmacy;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {

    EditText edtName;
    EditText edtEmail;
    EditText edtPhone;
    EditText edtPassword;
    EditText edtConfirmPassword;

    Button btnRegister;
    Button btnBack;

    TextView txtLogin;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_registration);

        // ==========================
        // FIREBASE
        // ==========================

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        // ==========================
        // FIND VIEWS
        // ==========================

        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);

        btnRegister = findViewById(R.id.btnRegister);
        btnBack = findViewById(R.id.btnBack);

        txtLogin = findViewById(R.id.txtLogin);


        // ==========================
        // REGISTER
        // ==========================

        btnRegister.setOnClickListener(v -> {

            String name =
                    edtName.getText().toString().trim();

            String email =
                    edtEmail.getText().toString().trim();

            String phone =
                    edtPhone.getText().toString().trim();

            String password =
                    edtPassword.getText().toString().trim();

            String confirmPassword =
                    edtConfirmPassword.getText().toString().trim();


            // ==========================
            // EMPTY FIELD CHECK
            // ==========================

            if (name.isEmpty()
                    || email.isEmpty()
                    || phone.isEmpty()
                    || password.isEmpty()
                    || confirmPassword.isEmpty()) {

                Toast.makeText(
                        RegistrationActivity.this,
                        "Please fill all fields",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }


            // ==========================
            // EMAIL CHECK
            // ==========================

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

                Toast.makeText(
                        RegistrationActivity.this,
                        "Please enter a valid email",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }


            // ==========================
            // PASSWORD LENGTH
            // ==========================

            if (password.length() < 6) {

                Toast.makeText(
                        RegistrationActivity.this,
                        "Password must be at least 6 characters",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }


            // ==========================
            // PASSWORD MATCH
            // ==========================

            if (!password.equals(confirmPassword)) {

                Toast.makeText(
                        RegistrationActivity.this,
                        "Passwords do not match",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }


            // ==========================
            // CREATE FIREBASE ACCOUNT
            // ==========================

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {

                        if (task.isSuccessful()) {

                            // Get Firebase User ID
                            String userId =
                                    mAuth.getCurrentUser().getUid();


                            // ==========================
                            // USER DATA
                            // ==========================

                            Map<String, Object> user = new HashMap<>();

                            user.put("name", name);
                            user.put("email", email);
                            user.put("phone", phone);


                            // ==========================
                            // SAVE USER TO FIRESTORE
                            // ==========================

                            db.collection("users")
                                    .document(userId)
                                    .set(user)
                                    .addOnSuccessListener(unused -> {

                                        Toast.makeText(
                                                RegistrationActivity.this,
                                                "Registration successful",
                                                Toast.LENGTH_SHORT
                                        ).show();


                                        // Go to Login
                                        Intent intent = new Intent(
                                                RegistrationActivity.this,
                                                LoginActivity.class
                                        );

                                        startActivity(intent);

                                        finish();

                                    })
                                    .addOnFailureListener(e -> {

                                        Toast.makeText(
                                                RegistrationActivity.this,
                                                "Account created, but profile could not be saved",
                                                Toast.LENGTH_LONG
                                        ).show();

                                    });

                        } else {

                            // ==========================
                            // REGISTRATION ERROR
                            // ==========================

                            String errorMessage;

                            if (task.getException() != null) {
                                errorMessage =
                                        task.getException().getMessage();
                            } else {
                                errorMessage =
                                        "Registration failed";
                            }

                            Toast.makeText(
                                    RegistrationActivity.this,
                                    errorMessage,
                                    Toast.LENGTH_LONG
                            ).show();
                        }

                    });

        });


        // ==========================
        // LOGIN
        // ==========================

        txtLogin.setOnClickListener(v -> {

            Intent intent = new Intent(
                    RegistrationActivity.this,
                    LoginActivity.class
            );

            startActivity(intent);

        });


        // ==========================
        // BACK
        // ==========================

        btnBack.setOnClickListener(v -> {

            finish();

        });

    }
}