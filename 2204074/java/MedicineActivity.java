package com.example.smartpharmacy;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MedicineActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Open Firebase medicine search page
        Intent intent = new Intent(
                MedicineActivity.this,
                SearchMedicineActivity.class
        );

        startActivity(intent);

        finish();
    }
}