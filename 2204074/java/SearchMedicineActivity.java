package com.example.smartpharmacy;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class SearchMedicineActivity extends AppCompatActivity {

    EditText edtSearchMedicine;
    Button btnSearchMedicine;
    Button btnShowAll;
    Button btnBack;

    LinearLayout medicineContainer;
    TextView txtNoMedicine;

    FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search_medicine);


        // ==========================
        // FIREBASE
        // ==========================

        db = FirebaseFirestore.getInstance();


        // ==========================
        // FIND VIEWS
        // ==========================

        edtSearchMedicine =
                findViewById(R.id.edtSearchMedicine);

        btnSearchMedicine =
                findViewById(R.id.btnSearchMedicine);

        btnShowAll =
                findViewById(R.id.btnShowAll);

        btnBack =
                findViewById(R.id.btnBack);

        medicineContainer =
                findViewById(R.id.medicineContainer);

        txtNoMedicine =
                findViewById(R.id.txtNoMedicine);


        // ==========================
        // SEARCH BUTTON
        // ==========================

        btnSearchMedicine.setOnClickListener(v -> {

            String searchText =
                    edtSearchMedicine.getText()
                            .toString()
                            .trim();

            if (searchText.isEmpty()) {

                Toast.makeText(
                        SearchMedicineActivity.this,
                        "Please enter medicine name",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            searchMedicine(searchText);
        });


        // ==========================
        // SHOW ALL MEDICINES
        // ==========================

        btnShowAll.setOnClickListener(v -> {

            edtSearchMedicine.setText("");

            loadAllMedicines();

        });


        // ==========================
        // BACK BUTTON
        // ==========================

        btnBack.setOnClickListener(v -> {

            finish();

        });

    }


    // =========================================================
    // SEARCH MEDICINE
    // =========================================================

    private void searchMedicine(String searchText) {

        medicineContainer.removeAllViews();

        txtNoMedicine.setVisibility(
                View.GONE
        );


        db.collection("medicines")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    // Firebase result আসার সময়
                    // পুরোনো card clear করে দিচ্ছি
                    medicineContainer.removeAllViews();

                    boolean found = false;

                    String searchLower =
                            searchText.toLowerCase();


                    for (QueryDocumentSnapshot document :
                            queryDocumentSnapshots) {


                        // ==========================
                        // GET DATA
                        // ==========================

                        String medicineId =
                                document.getId();


                        String name =
                                getStringValue(
                                        document.get("name")
                                );


                        String category =
                                getStringValue(
                                        document.get("category")
                                );


                        String company =
                                getStringValue(
                                        document.get("company")
                                );


                        String price =
                                getStringValue(
                                        document.get("price")
                                );


                        String quantity =
                                getStringValue(
                                        document.get("quantity")
                                );


                        // ==========================
                        // SEARCH
                        // ==========================

                        if (name.toLowerCase()
                                .contains(searchLower)

                                ||

                                category.toLowerCase()
                                        .contains(searchLower)

                                ||

                                company.toLowerCase()
                                        .contains(searchLower)) {


                            addMedicineCard(
                                    medicineId,
                                    name,
                                    category,
                                    price,
                                    quantity,
                                    company
                            );


                            found = true;
                        }

                    }


                    // ==========================
                    // NO RESULT
                    // ==========================

                    if (!found) {

                        txtNoMedicine.setText(
                                "No medicine found"
                        );

                        txtNoMedicine.setVisibility(
                                View.VISIBLE
                        );
                    }

                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            SearchMedicineActivity.this,
                            "Failed to search medicines: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();

                });

    }


    // =========================================================
    // LOAD ALL MEDICINES
    // =========================================================

    private void loadAllMedicines() {

        // ==========================
        // CLEAR OLD MEDICINES
        // ==========================

        medicineContainer.removeAllViews();

        txtNoMedicine.setVisibility(
                View.GONE
        );


        // ==========================
        // GET MEDICINES
        // ==========================

        db.collection("medicines")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    // Firebase response আসার পর
                    // আবার clear করছি
                    // যাতে duplicate card না থাকে

                    medicineContainer.removeAllViews();


                    // ==========================
                    // EMPTY DATABASE
                    // ==========================

                    if (queryDocumentSnapshots.isEmpty()) {

                        txtNoMedicine.setText(
                                "No medicines available"
                        );

                        txtNoMedicine.setVisibility(
                                View.VISIBLE
                        );

                        return;
                    }


                    // ==========================
                    // GET EVERY MEDICINE
                    // ==========================

                    for (QueryDocumentSnapshot document :
                            queryDocumentSnapshots) {


                        String medicineId =
                                document.getId();


                        String name =
                                getStringValue(
                                        document.get("name")
                                );


                        String category =
                                getStringValue(
                                        document.get("category")
                                );


                        String price =
                                getStringValue(
                                        document.get("price")
                                );


                        String quantity =
                                getStringValue(
                                        document.get("quantity")
                                );


                        String company =
                                getStringValue(
                                        document.get("company")
                                );


                        // ==========================
                        // ADD MEDICINE CARD
                        // ==========================

                        addMedicineCard(
                                medicineId,
                                name,
                                category,
                                price,
                                quantity,
                                company
                        );

                    }

                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            SearchMedicineActivity.this,
                            "Failed to load medicines: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();

                });

    }


    // =========================================================
    // CONVERT FIREBASE VALUE TO STRING
    // =========================================================

    private String getStringValue(Object value) {

        if (value == null) {

            return "";

        }

        return String.valueOf(value);

    }


    // =========================================================
    // MEDICINE CARD
    // =========================================================

    private void addMedicineCard(
            String medicineId,
            String name,
            String category,
            String price,
            String quantity,
            String company) {


        // ==========================
        // CREATE CARD
        // ==========================

        LinearLayout card =
                new LinearLayout(
                        SearchMedicineActivity.this
                );


        card.setOrientation(
                LinearLayout.VERTICAL
        );


        card.setPadding(
                20,
                20,
                20,
                20
        );


        // ==========================
        // CARD PARAMETERS
        // ==========================

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );


        cardParams.setMargins(
                0,
                0,
                0,
                20
        );


        card.setLayoutParams(
                cardParams
        );


        card.setBackgroundColor(
                Color.WHITE
        );


        // ==========================
        // MEDICINE NAME
        // ==========================

        TextView txtName =
                new TextView(
                        SearchMedicineActivity.this
                );


        txtName.setText(
                "💊 " + name
        );


        txtName.setTextSize(
                20
        );


        txtName.setTextColor(
                Color.rgb(23, 74, 69)
        );


        txtName.setTypeface(
                null,
                Typeface.BOLD
        );


        // ==========================
        // CATEGORY
        // ==========================

        TextView txtCategory =
                new TextView(
                        SearchMedicineActivity.this
                );


        txtCategory.setText(
                "Category: " + category
        );


        txtCategory.setTextSize(
                15
        );


        // ==========================
        // PRICE
        // ==========================

        TextView txtPrice =
                new TextView(
                        SearchMedicineActivity.this
                );


        txtPrice.setText(
                "Price: ৳" + price
        );


        txtPrice.setTextSize(
                15
        );


        // ==========================
        // QUANTITY
        // ==========================

        TextView txtQuantity =
                new TextView(
                        SearchMedicineActivity.this
                );


        txtQuantity.setText(
                "Available Quantity: " + quantity
        );


        txtQuantity.setTextSize(
                15
        );


        // ==========================
        // COMPANY
        // ==========================

        TextView txtCompany =
                new TextView(
                        SearchMedicineActivity.this
                );


        txtCompany.setText(
                "Company: " + company
        );


        txtCompany.setTextSize(
                15
        );


        // ==========================
        // ADD VIEWS TO CARD
        // ==========================

        card.addView(
                txtName
        );


        card.addView(
                txtCategory
        );


        card.addView(
                txtPrice
        );


        card.addView(
                txtQuantity
        );


        card.addView(
                txtCompany
        );


        // =====================================================
        // CARD CLICK
        // =====================================================

        card.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            SearchMedicineActivity.this,
                            MedicineDetailsActivity.class
                    );


            // ==========================
            // SEND MEDICINE DATA
            // ==========================

            intent.putExtra(
                    "medicineId",
                    medicineId
            );


            intent.putExtra(
                    "medicineName",
                    name
            );


            intent.putExtra(
                    "medicineCategory",
                    category
            );


            intent.putExtra(
                    "medicinePrice",
                    price
            );


            intent.putExtra(
                    "medicineQuantity",
                    quantity
            );


            intent.putExtra(
                    "medicineCompany",
                    company
            );


            // ==========================
            // OPEN DETAILS
            // ==========================

            startActivity(intent);

        });


        // ==========================
        // ADD CARD
        // ==========================

        medicineContainer.addView(
                card
        );

    }


    // =========================================================
    // RELOAD WHEN RETURNING
    // =========================================================

    @Override
    protected void onResume() {

        super.onResume();


        if (db != null &&
                medicineContainer != null) {

            loadAllMedicines();

        }

    }

}