package com.example.smartpharmacy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.json.JSONArray;
import org.json.JSONObject;

public class MedicineDetailsActivity extends AppCompatActivity {

    private TextView txtMedicineName;
    private TextView txtMedicinePrice;
    private TextView txtMedicineQuantity;

    private Button btnAddToCart;
    private Button btnBack;

    private FirebaseFirestore db;

    private String medicineId = "";
    private String medicineName = "";
    private String medicinePrice = "0";

    private int availableStock = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_medicine_details);

        // ==========================
        // FIREBASE
        // ==========================

        db = FirebaseFirestore.getInstance();

        // ==========================
        // FIND VIEWS
        // ==========================

        txtMedicineName =
                findViewById(R.id.txtMedicineName);

        txtMedicinePrice =
                findViewById(R.id.txtMedicinePrice);

        txtMedicineQuantity =
                findViewById(R.id.txtMedicineQuantity);

        btnAddToCart =
                findViewById(R.id.btnAddToCart);

        btnBack =
                findViewById(R.id.btnBack);

        // ==========================
        // GET DATA FROM INTENT
        // ==========================

        medicineId =
                getIntent().getStringExtra(
                        "medicineId"
                );

        medicineName =
                getIntent().getStringExtra(
                        "medicineName"
                );

        medicinePrice =
                getIntent().getStringExtra(
                        "medicinePrice"
                );

        // ==========================
        // DEFAULT VALUES
        // ==========================

        if (medicineId == null) {
            medicineId = "";
        }

        if (medicineName == null) {
            medicineName = "";
        }

        if (medicinePrice == null) {
            medicinePrice = "0";
        }

        medicineId = medicineId.trim();
        medicineName = medicineName.trim();

        // ==========================
        // INITIAL DISPLAY
        // ==========================

        txtMedicineName.setText(
                medicineName
        );

        txtMedicinePrice.setText(
                "Price: ৳" +
                        medicinePrice
        );

        txtMedicineQuantity.setText(
                "Available Quantity: Loading..."
        );

        btnAddToCart.setEnabled(false);

        // ==========================
        // LOAD LATEST MEDICINE
        // ==========================

        loadMedicine();

        // ==========================
        // ADD TO CART
        // ==========================

        btnAddToCart.setOnClickListener(v ->
                addToCart()
        );

        // ==========================
        // BACK
        // ==========================

        btnBack.setOnClickListener(v ->
                finish()
        );
    }

    // ==================================================
    // LOAD MEDICINE
    // ==================================================

    private void loadMedicine() {

        // ==================================================
        // OPTION 1
        // medicineId পাওয়া গেলে সরাসরি document load
        // ==================================================

        if (!medicineId.isEmpty()) {

            db.collection("medicines")
                    .document(medicineId)
                    .get()
                    .addOnSuccessListener(document -> {

                        if (document.exists()) {

                            readMedicineData(
                                    document
                            );

                        } else {

                            // ID ভুল হলে name দিয়ে খুঁজবে
                            findMedicineByName();
                        }
                    })
                    .addOnFailureListener(e -> {

                        findMedicineByName();
                    });

            return;
        }

        // ==================================================
        // OPTION 2
        // medicineId না থাকলে name দিয়ে search
        // ==================================================

        findMedicineByName();
    }

    // ==================================================
    // FIND MEDICINE BY NAME
    // ==================================================

    private void findMedicineByName() {

        if (medicineName.isEmpty()) {

            Toast.makeText(
                    this,
                    "Medicine information not found",
                    Toast.LENGTH_LONG
            ).show();

            txtMedicineQuantity.setText(
                    "Available Quantity: 0"
            );

            return;
        }

        db.collection("medicines")
                .whereEqualTo(
                        "name",
                        medicineName
                )
                .limit(1)
                .get()
                .addOnSuccessListener(
                        queryDocumentSnapshots -> {

                            if (queryDocumentSnapshots.isEmpty()) {

                                Toast.makeText(
                                        MedicineDetailsActivity.this,
                                        "Medicine not found",
                                        Toast.LENGTH_LONG
                                ).show();

                                txtMedicineQuantity.setText(
                                        "Available Quantity: 0"
                                );

                                return;
                            }

                            readMedicineData(
                                    queryDocumentSnapshots
                                            .getDocuments()
                                            .get(0)
                            );
                        }
                )
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            MedicineDetailsActivity.this,
                            "Failed to load medicine: " +
                                    e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();

                    txtMedicineQuantity.setText(
                            "Available Quantity: 0"
                    );
                });
    }

    // ==================================================
    // READ MEDICINE DATA
    // ==================================================

    private void readMedicineData(
            com.google.firebase.firestore.DocumentSnapshot document
    ) {

        // ==========================
        // DOCUMENT ID
        // ==========================

        medicineId =
                document.getId();

        // ==========================
        // NAME
        // ==========================

        String name =
                document.getString(
                        "name"
                );

        if (name != null &&
                !name.trim().isEmpty()) {

            medicineName =
                    name;
        }

        // ==========================
        // PRICE
        // ==========================

        Object priceObject =
                document.get(
                        "price"
                );

        if (priceObject instanceof Number) {

            double price =
                    ((Number)
                            priceObject)
                            .doubleValue();

            medicinePrice =
                    String.valueOf(
                            price
                    );

        } else if (priceObject != null) {

            medicinePrice =
                    String.valueOf(
                            priceObject
                    );
        }

        // ==========================
        // QUANTITY
        // ==========================

        Object quantityObject =
                document.get(
                        "quantity"
                );

        availableStock = 0;

        if (quantityObject
                instanceof Number) {

            availableStock =
                    ((Number)
                            quantityObject)
                            .intValue();
        }

        // ==========================
        // UPDATE UI
        // ==========================

        txtMedicineName.setText(
                medicineName
        );

        txtMedicinePrice.setText(
                "Price: ৳" +
                        medicinePrice
        );

        txtMedicineQuantity.setText(
                "Available Quantity: " +
                        availableStock
        );

        // ==========================
        // STOCK STATUS
        // ==========================

        if (availableStock <= 0) {

            btnAddToCart.setEnabled(
                    false
            );

            btnAddToCart.setText(
                    "Out of Stock"
            );

        } else {

            btnAddToCart.setEnabled(
                    true
            );

            btnAddToCart.setText(
                    "Add to Cart"
            );
        }
    }

    // ==================================================
    // ADD TO CART
    // ==================================================

    private void addToCart() {

        if (availableStock <= 0) {

            Toast.makeText(
                    this,
                    "Medicine is out of stock",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        try {

            SharedPreferences prefs =
                    getSharedPreferences(
                            "SmartPharmacyCart",
                            MODE_PRIVATE
                    );

            String cartJson =
                    prefs.getString(
                            "cart",
                            "[]"
                    );

            JSONArray cart =
                    new JSONArray(
                            cartJson
                    );

            boolean alreadyExists = false;

            // ==================================================
            // CHECK EXISTING MEDICINE
            // ==================================================

            for (int i = 0;
                 i < cart.length();
                 i++) {

                JSONObject item =
                        cart.getJSONObject(i);

                String existingId =
                        item.optString(
                                "medicineId",
                                ""
                        );

                String existingName =
                        item.optString(
                                "name",
                                ""
                        );

                boolean sameMedicine =
                        (!medicineId.isEmpty()
                                &&
                                existingId.equals(
                                        medicineId
                                ))
                                ||
                                existingName.equalsIgnoreCase(
                                        medicineName
                                );

                if (sameMedicine) {

                    int currentQuantity =
                            item.optInt(
                                    "quantity",
                                    1
                            );

                    // ==================================================
                    // STOCK LIMIT
                    // ==================================================

                    if (currentQuantity >=
                            availableStock) {

                        Toast.makeText(
                                MedicineDetailsActivity.this,
                                "Maximum available stock reached",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    item.put(
                            "quantity",
                            currentQuantity + 1
                    );

                    item.put(
                            "medicineId",
                            medicineId
                    );

                    item.put(
                            "name",
                            medicineName
                    );

                    item.put(
                            "price",
                            medicinePrice
                    );

                    item.put(
                            "availableStock",
                            availableStock
                    );

                    alreadyExists = true;

                    break;
                }
            }

            // ==================================================
            // ADD NEW MEDICINE
            // ==================================================

            if (!alreadyExists) {

                JSONObject medicine =
                        new JSONObject();

                medicine.put(
                        "medicineId",
                        medicineId
                );

                medicine.put(
                        "name",
                        medicineName
                );

                medicine.put(
                        "price",
                        medicinePrice
                );

                medicine.put(
                        "quantity",
                        1
                );

                medicine.put(
                        "availableStock",
                        availableStock
                );

                cart.put(
                        medicine
                );
            }

            // ==================================================
            // SAVE CART
            // ==================================================

            prefs.edit()
                    .putString(
                            "cart",
                            cart.toString()
                    )
                    .apply();

            Toast.makeText(
                    MedicineDetailsActivity.this,
                    medicineName +
                            " added to cart",
                    Toast.LENGTH_SHORT
            ).show();

            // ==================================================
            // OPEN CART
            // ==================================================

            Intent intent =
                    new Intent(
                            MedicineDetailsActivity.this,
                            CartActivity.class
                    );

            startActivity(intent);

        } catch (Exception e) {

            Toast.makeText(
                    MedicineDetailsActivity.this,
                    "Unable to add medicine to cart",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}