package com.example.smartpharmacy;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddMedicineActivity extends AppCompatActivity {

    EditText edtMedicineName;
    EditText edtCategory;
    EditText edtPrice;
    EditText edtQuantity;
    EditText edtCompany;

    Button btnAddMedicine;
    Button btnBack;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_medicine);

        // ==========================
        // FIREBASE
        // ==========================

        db = FirebaseFirestore.getInstance();

        // ==========================
        // FIND VIEWS
        // ==========================

        edtMedicineName = findViewById(R.id.edtMedicineName);
        edtCategory = findViewById(R.id.edtCategory);
        edtPrice = findViewById(R.id.edtPrice);
        edtQuantity = findViewById(R.id.edtQuantity);
        edtCompany = findViewById(R.id.edtCompany);

        btnAddMedicine = findViewById(R.id.btnAddMedicine);
        btnBack = findViewById(R.id.btnBack);

        // ==========================
        // ADD MEDICINE
        // ==========================

        btnAddMedicine.setOnClickListener(v -> {

            String name = edtMedicineName.getText().toString().trim();
            String category = edtCategory.getText().toString().trim();
            String priceText = edtPrice.getText().toString().trim();
            String quantityText = edtQuantity.getText().toString().trim();
            String company = edtCompany.getText().toString().trim();

            // ==========================
            // VALIDATION
            // ==========================

            if (name.isEmpty()
                    || category.isEmpty()
                    || priceText.isEmpty()
                    || quantityText.isEmpty()
                    || company.isEmpty()) {

                Toast.makeText(
                        AddMedicineActivity.this,
                        "Please fill all fields",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            // ==========================
            // CONVERT PRICE & QUANTITY
            // ==========================

            double price;

            int quantity;

            try {

                price = Double.parseDouble(priceText);

            } catch (NumberFormatException e) {

                Toast.makeText(
                        AddMedicineActivity.this,
                        "Please enter a valid price",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            try {

                quantity = Integer.parseInt(quantityText);

            } catch (NumberFormatException e) {

                Toast.makeText(
                        AddMedicineActivity.this,
                        "Please enter a valid quantity",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            // ==========================
            // CREATE MEDICINE DATA
            // ==========================

            Map<String, Object> medicine = new HashMap<>();

            medicine.put("name", name);
            medicine.put("category", category);
            medicine.put("price", price);
            medicine.put("quantity", quantity);
            medicine.put("company", company);

            // ==========================
            // SAVE TO FIRESTORE
            // DOCUMENT ID = MEDICINE NAME
            // ==========================

            db.collection("medicines")
                    .document(name)
                    .set(medicine)
                    .addOnSuccessListener(unused -> {

                        Toast.makeText(
                                AddMedicineActivity.this,
                                "Medicine added successfully",
                                Toast.LENGTH_SHORT
                        ).show();

                        // Clear fields

                        edtMedicineName.setText("");
                        edtCategory.setText("");
                        edtPrice.setText("");
                        edtQuantity.setText("");
                        edtCompany.setText("");

                    })
                    .addOnFailureListener(e -> {

                        Toast.makeText(
                                AddMedicineActivity.this,
                                "Failed to add medicine: "
                                        + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();

                    });
        });

        // ==========================
        // BACK BUTTON
        // ==========================

        btnBack.setOnClickListener(v -> finish());
    }
}