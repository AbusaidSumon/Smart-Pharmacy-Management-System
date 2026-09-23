package com.example.smartpharmacy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    Button btnUserLogin;
    Button btnUserRegistration;
    Button btnAdminLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // ==========================
        // USER BUTTONS
        // ==========================

        btnUserLogin = findViewById(R.id.btnUserLogin);

        btnUserRegistration = findViewById(R.id.btnUserRegistration);


        // ==========================
        // ADMIN / PHARMACIST LOGIN
        // ==========================

        btnAdminLogin = findViewById(R.id.btnAdminLogin);


        // ==========================
        // USER LOGIN
        // ==========================

        btnUserLogin.setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    LoginActivity.class
            );

            startActivity(intent);

        });


        // ==========================
        // USER REGISTRATION
        // ==========================

        btnUserRegistration.setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    RegistrationActivity.class
            );

            startActivity(intent);

        });


        // ==========================
        // ADMIN / PHARMACIST LOGIN
        // ==========================

        btnAdminLogin.setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    AdminLoginActivity.class
            );

            startActivity(intent);

        });

    }
}