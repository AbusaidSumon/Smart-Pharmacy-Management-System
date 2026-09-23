package com.example.smartpharmacy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    EditText edtEmail;
    EditText edtPassword;

    CheckBox chkRememberMe;

    Button btnLogin;
    Button btnBack;

    TextView txtRegister;
    TextView txtForgotPassword;

    ImageButton btnShowPassword;

    FirebaseAuth mAuth;

    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);


        // ==========================
        // FIREBASE AUTH
        // ==========================

        mAuth = FirebaseAuth.getInstance();


        // ==========================
        // SHARED PREFERENCES
        // ==========================

        sharedPreferences =
                getSharedPreferences(
                        "SmartPharmacyLogin",
                        MODE_PRIVATE
                );


        // ==========================
        // FIND VIEWS
        // ==========================

        edtEmail =
                findViewById(R.id.edtEmail);

        edtPassword =
                findViewById(R.id.edtPassword);

        chkRememberMe =
                findViewById(R.id.chkRememberMe);

        btnLogin =
                findViewById(R.id.btnLogin);

        btnBack =
                findViewById(R.id.btnBack);

        txtRegister =
                findViewById(R.id.txtRegister);

        txtForgotPassword =
                findViewById(R.id.txtForgotPassword);

        btnShowPassword =
                findViewById(R.id.btnShowPassword);


        // ==========================
        // PASSWORD SHOW / HIDE
        // ==========================

        btnShowPassword.setOnClickListener(v -> {

            int cursorPosition =
                    edtPassword.getSelectionStart();


            // ==========================
            // CURRENTLY PASSWORD HIDDEN
            // ==========================

            if (
                    edtPassword.getTransformationMethod()
                            instanceof PasswordTransformationMethod
            ) {

                // SHOW PASSWORD

                edtPassword.setTransformationMethod(
                        HideReturnsTransformationMethod
                                .getInstance()
                );

                btnShowPassword.setContentDescription(
                        "Hide password"
                );


            } else {

                // ==========================
                // HIDE PASSWORD
                // ==========================

                edtPassword.setTransformationMethod(
                        PasswordTransformationMethod
                                .getInstance()
                );

                btnShowPassword.setContentDescription(
                        "Show password"
                );
            }


            // ==========================
            // KEEP CURSOR POSITION
            // ==========================

            if (cursorPosition >= 0) {

                edtPassword.setSelection(
                        Math.min(
                                cursorPosition,
                                edtPassword.length()
                        )
                );
            }

        });


        // ==========================
        // LOAD REMEMBERED EMAIL
        // ==========================

        boolean rememberMe =
                sharedPreferences.getBoolean(
                        "rememberMe",
                        false
                );

        if (rememberMe) {

            String savedEmail =
                    sharedPreferences.getString(
                            "email",
                            ""
                    );

            edtEmail.setText(savedEmail);

            chkRememberMe.setChecked(true);
        }


        // ==========================
        // LOGIN
        // ==========================

        btnLogin.setOnClickListener(v -> {

            String email =
                    edtEmail.getText()
                            .toString()
                            .trim();

            String password =
                    edtPassword.getText()
                            .toString()
                            .trim();


            // ==========================
            // EMAIL EMPTY CHECK
            // ==========================

            if (email.isEmpty()) {

                edtEmail.setError(
                        "Enter email"
                );

                edtEmail.requestFocus();

                return;
            }


            // ==========================
            // EMAIL VALIDATION
            // ==========================

            if (!Patterns.EMAIL_ADDRESS
                    .matcher(email)
                    .matches()) {

                edtEmail.setError(
                        "Enter valid email"
                );

                edtEmail.requestFocus();

                return;
            }


            // ==========================
            // PASSWORD CHECK
            // ==========================

            if (password.isEmpty()) {

                edtPassword.setError(
                        "Enter password"
                );

                edtPassword.requestFocus();

                return;
            }


            // ==========================
            // LOGIN BUTTON DISABLE
            // ==========================

            btnLogin.setEnabled(false);


            // ==========================
            // FIREBASE LOGIN
            // ==========================

            mAuth.signInWithEmailAndPassword(
                            email,
                            password
                    )
                    .addOnCompleteListener(task -> {

                        btnLogin.setEnabled(true);


                        if (task.isSuccessful()) {


                            // ==========================
                            // REMEMBER ME
                            // ==========================

                            if (chkRememberMe.isChecked()) {

                                sharedPreferences
                                        .edit()
                                        .putBoolean(
                                                "rememberMe",
                                                true
                                        )
                                        .putString(
                                                "email",
                                                email
                                        )
                                        .apply();

                            } else {

                                sharedPreferences
                                        .edit()
                                        .clear()
                                        .apply();
                            }


                            // ==========================
                            // LOGIN SUCCESS
                            // ==========================

                            Toast.makeText(
                                    LoginActivity.this,
                                    "Login successful",
                                    Toast.LENGTH_SHORT
                            ).show();


                            Intent intent =
                                    new Intent(
                                            LoginActivity.this,
                                            UserDashboardActivity.class
                                    );

                            startActivity(intent);

                            finish();


                        } else {

                            // ==========================
                            // LOGIN FAILED
                            // ==========================

                            String errorMessage =
                                    "Login failed";

                            if (task.getException() != null) {

                                errorMessage =
                                        task.getException()
                                                .getMessage();
                            }

                            Toast.makeText(
                                    LoginActivity.this,
                                    errorMessage,
                                    Toast.LENGTH_LONG
                            ).show();
                        }

                    });

        });


        // ==========================
        // REGISTRATION
        // ==========================

        txtRegister.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            LoginActivity.this,
                            RegistrationActivity.class
                    );

            startActivity(intent);

        });


        // ==========================
        // FORGOT PASSWORD
        // ==========================

        txtForgotPassword.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            LoginActivity.this,
                            ForgotPasswordActivity.class
                    );

            startActivity(intent);

        });


        // ==========================
        // BACK
        // ==========================

        btnBack.setOnClickListener(v -> finish());

    }
}