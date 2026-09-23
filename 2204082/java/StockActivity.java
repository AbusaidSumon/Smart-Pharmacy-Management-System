package com.example.smartpharmacy;

import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class StockActivity extends AppCompatActivity {

    // =========================
    // Variables
    // =========================

    private LinearLayout stockContainer;
    private Button btnBack;

    private FirebaseFirestore db;

    // =========================
    // Colors
    // =========================

    private final int DARK_GREEN =
            Color.rgb(23, 74, 69);

    private final int GREEN =
            Color.rgb(40, 124, 112);

    private final int LIGHT_GREEN =
            Color.rgb(225, 239, 236);

    private final int BROWN =
            Color.rgb(118, 85, 45);

    private final int RED =
            Color.rgb(190, 50, 50);

    private final int YELLOW =
            Color.rgb(180, 120, 20);

    private final int SUCCESS_GREEN =
            Color.rgb(40, 130, 70);

    private final int TEXT_COLOR =
            Color.rgb(70, 70, 70);

    // =========================
    // onCreate
    // =========================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_stock);

        // Firebase
        db = FirebaseFirestore.getInstance();

        // XML Views
        stockContainer =
                findViewById(R.id.stockContainer);

        btnBack =
                findViewById(R.id.btnBack);

        // Back
        btnBack.setOnClickListener(
                v -> finish()
        );

        // Load medicines
        loadMedicines();
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
    // Load Medicines
    // =========================

    private void loadMedicines() {

        stockContainer.removeAllViews();

        db.collection("medicines")
                .get()
                .addOnSuccessListener(
                        queryDocumentSnapshots -> {

                            if (queryDocumentSnapshots.isEmpty()) {

                                showMessage(
                                        "No medicines found."
                                );

                                return;
                            }

                            for (
                                    DocumentSnapshot document :
                                    queryDocumentSnapshots
                            ) {

                                addMedicineCard(
                                        document
                                );
                            }
                        }
                )
                .addOnFailureListener(
                        e -> {

                            showMessage(
                                    "Failed to load medicines."
                            );

                            Toast.makeText(
                                    StockActivity.this,
                                    e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    // =========================
    // Medicine Card
    // =========================

    private void addMedicineCard(
            DocumentSnapshot document
    ) {

        // IMPORTANT:
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

        // Current quantity
        int currentQuantity =
                getSafeQuantity(document);

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

        txtName.setTextSize(22);
        txtName.setTextColor(
                DARK_GREEN
        );

        card.addView(txtName);

        // =========================
        // Current Stock
        // =========================

        TextView txtStock =
                new TextView(this);

        txtStock.setText(
                "Current Stock: "
                        + currentQuantity
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

        card.addView(txtStock);

        // =========================
        // Status
        // =========================

        TextView txtStatus =
                new TextView(this);

        txtStatus.setTextSize(16);

        setStatus(
                txtStatus,
                currentQuantity
        );

        LinearLayout.LayoutParams statusParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        statusParams.setMargins(
                0,
                dp(2),
                0,
                dp(6)
        );

        txtStatus.setLayoutParams(
                statusParams
        );

        card.addView(txtStatus);

        // =========================
        // Button Row
        // =========================

        LinearLayout buttonRow =
                new LinearLayout(this);

        buttonRow.setOrientation(
                LinearLayout.HORIZONTAL
        );

        buttonRow.setGravity(
                Gravity.CENTER_VERTICAL
        );

        // Add Button
        Button btnAdd =
                createButton(
                        "+ Add",
                        GREEN
                );

        // Remove Button
        Button btnRemove =
                createButton(
                        "− Remove",
                        RED
                );

        // Add button params
        LinearLayout.LayoutParams addParams =
                new LinearLayout.LayoutParams(
                        0,
                        dp(42),
                        1
                );

        buttonRow.addView(
                btnAdd,
                addParams
        );

        // Remove button params
        LinearLayout.LayoutParams removeParams =
                new LinearLayout.LayoutParams(
                        0,
                        dp(42),
                        1
                );

        removeParams.setMargins(
                dp(6),
                0,
                0,
                0
        );

        buttonRow.addView(
                btnRemove,
                removeParams
        );

        card.addView(
                buttonRow
        );

        // =========================
        // Set Exact Button
        // =========================

        Button btnSetExact =
                createButton(
                        "Set Exact Stock",
                        BROWN
                );

        LinearLayout.LayoutParams exactParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(42)
                );

        exactParams.setMargins(
                0,
                dp(6),
                0,
                0
        );

        btnSetExact.setLayoutParams(
                exactParams
        );

        card.addView(
                btnSetExact
        );

        // Add card
        stockContainer.addView(
                card
        );

        // =========================
        // Final Values
        // =========================

        final String finalDocumentId =
                documentId;

        final String finalMedicineName =
                medicineName;

        // =========================
        // Add Stock
        // =========================

        btnAdd.setOnClickListener(
                v -> showQuantityDialog(
                        finalDocumentId,
                        finalMedicineName,
                        "Add Stock",
                        "Quantity to Add",
                        true,
                        false
                )
        );

        // =========================
        // Remove Stock
        // =========================

        btnRemove.setOnClickListener(
                v -> showQuantityDialog(
                        finalDocumentId,
                        finalMedicineName,
                        "Remove Stock",
                        "Quantity to Remove",
                        false,
                        false
                )
        );

        // =========================
        // Set Exact Stock
        // =========================

        btnSetExact.setOnClickListener(
                v -> showQuantityDialog(
                        finalDocumentId,
                        finalMedicineName,
                        "Set Exact Stock",
                        "New Stock Quantity",
                        false,
                        true
                )
        );
    }

    // =========================
    // Create Button
    // =========================

    private Button createButton(
            String text,
            int backgroundColor
    ) {

        Button button =
                new Button(this);

        button.setText(text);

        button.setTextSize(14);

        button.setTextColor(
                Color.WHITE
        );

        button.setGravity(
                Gravity.CENTER
        );

        button.setAllCaps(false);

        button.setPadding(
                dp(3),
                0,
                dp(3),
                0
        );

        button.setMinHeight(0);
        button.setMinimumHeight(0);

        button.setBackgroundTintList(
                ColorStateList.valueOf(
                        backgroundColor
                )
        );

        return button;
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
    // Status
    // =========================

    private void setStatus(
            TextView statusText,
            int quantity
    ) {

        if (quantity == 0) {

            statusText.setText(
                    "Status: OUT OF STOCK"
            );

            statusText.setTextColor(
                    RED
            );

        } else if (quantity <= 10) {

            statusText.setText(
                    "Status: LOW STOCK"
            );

            statusText.setTextColor(
                    YELLOW
            );

        } else {

            statusText.setText(
                    "Status: IN STOCK"
            );

            statusText.setTextColor(
                    SUCCESS_GREEN
            );
        }
    }

    // =========================
    // Quantity Dialog
    // =========================

    private void showQuantityDialog(
            String documentId,
            String medicineName,
            String title,
            String hint,
            boolean isAdd,
            boolean isExact
    ) {

        EditText input =
                new EditText(this);

        input.setHint(hint);

        input.setTextSize(16);

        input.setInputType(
                InputType.TYPE_CLASS_NUMBER
        );

        LinearLayout layout =
                new LinearLayout(this);

        layout.setOrientation(
                LinearLayout.VERTICAL
        );

        layout.setPadding(
                dp(25),
                dp(5),
                dp(25),
                dp(5)
        );

        layout.addView(
                input
        );

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle(
                                title
                                        + " - "
                                        + medicineName
                        )
                        .setView(layout)
                        .setNegativeButton(
                                "Cancel",
                                null
                        )
                        .setPositiveButton(
                                "Update",
                                null
                        )
                        .create();

        dialog.setOnShowListener(
                dialogInterface -> {

                    Button updateButton =
                            dialog.getButton(
                                    AlertDialog.BUTTON_POSITIVE
                            );

                    updateButton.setOnClickListener(
                            v -> {

                                String value =
                                        input.getText()
                                                .toString()
                                                .trim();

                                if (
                                        value.isEmpty()
                                ) {

                                    input.setError(
                                            "Enter quantity"
                                    );

                                    return;
                                }

                                int amount;

                                try {

                                    amount =
                                            Integer.parseInt(
                                                    value
                                            );

                                } catch (
                                        NumberFormatException e
                                ) {

                                    input.setError(
                                            "Invalid quantity"
                                    );

                                    return;
                                }

                                if (
                                        amount < 0
                                ) {

                                    input.setError(
                                            "Quantity cannot be negative"
                                    );

                                    return;
                                }

                                if (
                                        !isExact &&
                                                amount == 0
                                ) {

                                    input.setError(
                                            "Enter quantity greater than 0"
                                    );

                                    return;
                                }

                                updateStock(
                                        documentId,
                                        medicineName,
                                        amount,
                                        isAdd,
                                        isExact,
                                        dialog
                                );
                            }
                    );
                }
        );

        dialog.show();
    }

    // =========================
    // Update Stock
    // =========================

    private void updateStock(
            String documentId,
            String medicineName,
            int amount,
            boolean isAdd,
            boolean isExact,
            AlertDialog dialog
    ) {

        /*
         * IMPORTANT:
         *
         * Medicine name নয়,
         * Firestore-এর আসল Document ID
         * দিয়ে document খোঁজা হচ্ছে।
         */

        db.collection("medicines")
                .document(documentId)
                .get()
                .addOnSuccessListener(
                        document -> {

                            if (
                                    !document.exists()
                            ) {

                                Toast.makeText(
                                        StockActivity.this,
                                        "Medicine document not found",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }

                            int currentQuantity =
                                    getSafeQuantity(
                                            document
                                    );

                            int newQuantity;

                            // =========================
                            // Set Exact
                            // =========================

                            if (isExact) {

                                newQuantity =
                                        amount;

                            }

                            // =========================
                            // Add
                            // =========================

                            else if (isAdd) {

                                newQuantity =
                                        currentQuantity
                                                + amount;
                            }

                            // =========================
                            // Remove
                            // =========================

                            else {

                                newQuantity =
                                        currentQuantity
                                                - amount;

                                if (
                                        newQuantity < 0
                                ) {

                                    Toast.makeText(
                                            StockActivity.this,
                                            "Stock cannot be negative. Current stock: "
                                                    + currentQuantity,
                                            Toast.LENGTH_LONG
                                    ).show();

                                    return;
                                }
                            }

                            // =========================
                            // Firebase Update
                            // =========================

                            db.collection("medicines")
                                    .document(documentId)
                                    .update(
                                            "quantity",
                                            newQuantity
                                    )
                                    .addOnSuccessListener(
                                            unused -> {

                                                dialog.dismiss();

                                                Toast.makeText(
                                                        StockActivity.this,
                                                        "Stock updated successfully",
                                                        Toast.LENGTH_SHORT
                                                ).show();

                                                // Reload list
                                                loadMedicines();
                                            }
                                    )
                                    .addOnFailureListener(
                                            e -> {

                                                Toast.makeText(
                                                        StockActivity.this,
                                                        "Update failed: "
                                                                + e.getMessage(),
                                                        Toast.LENGTH_LONG
                                                ).show();
                                            }
                                    );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            Toast.makeText(
                                    StockActivity.this,
                                    "Failed to get stock: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    // =========================
    // Empty/Error Message
    // =========================

    private void showMessage(
            String message
    ) {

        stockContainer.removeAllViews();

        TextView textView =
                new TextView(this);

        textView.setText(
                message
        );

        textView.setTextSize(16);

        textView.setTextColor(
                DARK_GREEN
        );

        textView.setGravity(
                Gravity.CENTER
        );

        textView.setPadding(
                dp(20),
                dp(30),
                dp(20),
                dp(30)
        );

        stockContainer.addView(
                textView
        );
    }
}