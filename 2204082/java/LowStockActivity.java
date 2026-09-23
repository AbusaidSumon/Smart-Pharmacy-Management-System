package com.example.smartpharmacy;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class LowStockActivity extends AppCompatActivity {

    // =========================
    // Variables
    // =========================

    private LinearLayout lowStockContainer;
    private Button btnRefresh;
    private Button btnBack;

    private FirebaseFirestore db;

    private ListenerRegistration medicineListener;

    // =========================
    // Colors
    // =========================

    private final int DARK_GREEN =
            Color.rgb(23, 74, 69);

    private final int LIGHT_GREEN =
            Color.rgb(225, 239, 236);

    private final int RED =
            Color.rgb(190, 50, 50);

    private final int YELLOW =
            Color.rgb(180, 120, 20);

    private final int TEXT_COLOR =
            Color.rgb(70, 70, 70);

    // =========================
    // onCreate
    // =========================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_low_stock
        );

        // Firebase
        db = FirebaseFirestore.getInstance();

        // XML Views
        lowStockContainer =
                findViewById(
                        R.id.lowStockContainer
                );

        btnRefresh =
                findViewById(
                        R.id.btnRefresh
                );

        btnBack =
                findViewById(
                        R.id.btnBack
                );

        // =========================
        // Back Button
        // =========================

        btnBack.setOnClickListener(
                v -> finish()
        );

        // =========================
        // Refresh Button
        // =========================

        btnRefresh.setOnClickListener(
                v -> loadLowStockMedicines()
        );

        // Initial load
        loadLowStockMedicines();
    }

    // =========================
    // DP Converter
    // =========================

    private int dp(int value) {

        return (int) (
                value *
                        getResources()
                                .getDisplayMetrics()
                                .density
                        + 0.5f
        );
    }

    // =========================
    // Load Low Stock Medicines
    // =========================

    private void loadLowStockMedicines() {

        /*
         * আগের listener থাকলে remove করি
         * যাতে একই data একাধিকবার load না হয়।
         */

        if (medicineListener != null) {

            medicineListener.remove();

            medicineListener = null;
        }

        /*
         * Realtime listener
         *
         * medicines collection-এ quantity
         * পরিবর্তন হলেই এই list automatically
         * update হবে।
         */

        medicineListener =
                db.collection("medicines")
                        .addSnapshotListener(
                                (querySnapshot, error) -> {

                                    if (error != null) {

                                        showMessage(
                                                "Failed to load medicines."
                                        );

                                        Toast.makeText(
                                                LowStockActivity.this,
                                                error.getMessage(),
                                                Toast.LENGTH_LONG
                                        ).show();

                                        return;
                                    }

                                    if (
                                            querySnapshot == null
                                    ) {

                                        showMessage(
                                                "No medicine data found."
                                        );

                                        return;
                                    }

                                    // Clear old list
                                    lowStockContainer
                                            .removeAllViews();

                                    boolean foundLowStock =
                                            false;

                                    // =========================
                                    // Check Every Medicine
                                    // =========================

                                    for (
                                            DocumentSnapshot document :
                                            querySnapshot
                                    ) {

                                        int quantity =
                                                getSafeQuantity(
                                                        document
                                                );

                                        /*
                                         * FINAL RULE:
                                         *
                                         * 0-10 = SHOW
                                         * 11+  = DON'T SHOW
                                         */

                                        if (
                                                quantity >= 0 &&
                                                        quantity <= 10
                                        ) {

                                            foundLowStock = true;

                                            addMedicineCard(
                                                    document,
                                                    quantity
                                            );
                                        }
                                    }

                                    // =========================
                                    // Nothing Found
                                    // =========================

                                    if (
                                            !foundLowStock
                                    ) {

                                        showNoLowStockMessage();
                                    }
                                }
                        );
    }

    // =========================
    // Medicine Card
    // =========================

    private void addMedicineCard(
            DocumentSnapshot document,
            int quantity
    ) {

        // Actual Firestore Document ID
        String documentId =
                document.getId();

        // Medicine name
        String medicineName =
                document.getString("name");

        if (
                medicineName == null ||
                        medicineName.trim().isEmpty()
        ) {

            medicineName =
                    documentId;
        }

        // =========================
        // Card
        // =========================

        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.VERTICAL
        );

        card.setPadding(
                dp(12),
                dp(10),
                dp(12),
                dp(10)
        );

        card.setBackgroundColor(
                LIGHT_GREEN
        );

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(
                0,
                0,
                0,
                dp(8)
        );

        card.setLayoutParams(
                cardParams
        );

        // =========================
        // Medicine Name
        // =========================

        TextView txtName =
                new TextView(this);

        txtName.setText(
                medicineName
        );

        txtName.setTextSize(21);

        txtName.setTextColor(
                DARK_GREEN
        );

        card.addView(
                txtName
        );

        // =========================
        // Current Stock
        // =========================

        TextView txtStock =
                new TextView(this);

        txtStock.setText(
                "Current Stock: "
                        + quantity
                        + " units"
        );

        txtStock.setTextSize(17);

        txtStock.setTextColor(
                TEXT_COLOR
        );

        LinearLayout.LayoutParams stockParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        stockParams.setMargins(
                0,
                dp(3),
                0,
                0
        );

        txtStock.setLayoutParams(
                stockParams
        );

        card.addView(
                txtStock
        );

        // =========================
        // Status
        // =========================

        TextView txtStatus =
                new TextView(this);

        txtStatus.setTextSize(16);

        if (quantity == 0) {

            txtStatus.setText(
                    "Status: OUT OF STOCK"
            );

            txtStatus.setTextColor(
                    RED
            );

        } else {

            txtStatus.setText(
                    "Status: LOW STOCK"
            );

            txtStatus.setTextColor(
                    YELLOW
            );
        }

        LinearLayout.LayoutParams statusParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        statusParams.setMargins(
                0,
                dp(2),
                0,
                0
        );

        txtStatus.setLayoutParams(
                statusParams
        );

        card.addView(
                txtStatus
        );

        // Add card to list
        lowStockContainer.addView(
                card
        );
    }

    // =========================
    // No Low Stock Message
    // =========================

    private void showNoLowStockMessage() {

        TextView message =
                new TextView(this);

        message.setText(
                "All medicines are sufficiently stocked."
        );

        message.setTextSize(16);

        message.setTextColor(
                DARK_GREEN
        );

        message.setGravity(
                Gravity.CENTER
        );

        message.setPadding(
                dp(20),
                dp(30),
                dp(20),
                dp(30)
        );

        lowStockContainer.addView(
                message
        );
    }

    // =========================
    // General Message
    // =========================

    private void showMessage(
            String messageText
    ) {

        lowStockContainer.removeAllViews();

        TextView message =
                new TextView(this);

        message.setText(
                messageText
        );

        message.setTextSize(16);

        message.setTextColor(
                TEXT_COLOR
        );

        message.setGravity(
                Gravity.CENTER
        );

        message.setPadding(
                dp(20),
                dp(30),
                dp(20),
                dp(30)
        );

        lowStockContainer.addView(
                message
        );
    }

    // =========================
    // Safe Quantity
    // =========================

    private int getSafeQuantity(
            DocumentSnapshot document
    ) {

        Object quantityObject =
                document.get("quantity");

        if (
                quantityObject instanceof Number
        ) {

            return ((Number) quantityObject)
                    .intValue();
        }

        return 0;
    }

    // =========================
    // Activity Destroy
    // =========================

    @Override
    protected void onDestroy() {

        super.onDestroy();

        if (
                medicineListener != null
        ) {

            medicineListener.remove();

            medicineListener = null;
        }
    }
}