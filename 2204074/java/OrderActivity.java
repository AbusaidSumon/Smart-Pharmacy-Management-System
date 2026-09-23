
package com.example.smartpharmacy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class OrderActivity extends AppCompatActivity {

    private static final double FIXED_DELIVERY_CHARGE = 60.00;

    private TextView txtOrderMedicine;
    private TextView txtOrderPrice;

    private EditText edtCustomerName;
    private EditText edtPhone;
    private EditText edtAddress;

    private Button btnConfirmOrder;
    private Button btnBack;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private JSONArray cart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_order);

        // ==========================
        // FIREBASE
        // ==========================

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // ==========================
        // FIND VIEWS
        // ==========================

        txtOrderMedicine =
                findViewById(R.id.txtOrderMedicine);

        txtOrderPrice =
                findViewById(R.id.txtOrderPrice);

        edtCustomerName =
                findViewById(R.id.edtCustomerName);

        edtPhone =
                findViewById(R.id.edtPhone);

        edtAddress =
                findViewById(R.id.edtAddress);

        btnConfirmOrder =
                findViewById(R.id.btnConfirmOrder);

        btnBack =
                findViewById(R.id.btnBack);

        // ==========================
        // GET CART DATA
        // ==========================

        String cartData =
                getIntent().getStringExtra("cartData");

        String medicineTotal =
                getIntent().getStringExtra("medicineTotal");

        String deliveryCharge =
                getIntent().getStringExtra("deliveryCharge");

        String grandTotal =
                getIntent().getStringExtra("grandTotal");

        if (medicineTotal == null ||
                medicineTotal.trim().isEmpty()) {
            medicineTotal = "0.00";
        }

        if (deliveryCharge == null ||
                deliveryCharge.trim().isEmpty()) {
            deliveryCharge = String.format(
                    java.util.Locale.getDefault(),
                    "%.2f",
                    FIXED_DELIVERY_CHARGE
            );
        }

        if (grandTotal == null ||
                grandTotal.trim().isEmpty()) {
            try {
                double medicine = Double.parseDouble(medicineTotal);
                double delivery = Double.parseDouble(deliveryCharge);
                grandTotal = String.format(
                        java.util.Locale.getDefault(),
                        "%.2f",
                        medicine + delivery
                );
            } catch (Exception e) {
                grandTotal = "60.00";
            }
        }

        if (cartData == null) {
            cartData = "[]";
        }

        if (grandTotal == null ||
                grandTotal.trim().isEmpty()) {

            grandTotal = "৳0.00";
        }

        // IMPORTANT:
        // Lambda-এর ভিতরে ব্যবহারের জন্য final variable
        final String finalMedicineTotal = medicineTotal;
        final String finalDeliveryCharge = deliveryCharge;
        final String finalGrandTotal = grandTotal;

        // ==========================
        // PARSE CART
        // ==========================

        try {

            cart = new JSONArray(cartData);

        } catch (Exception e) {

            cart = new JSONArray();
        }

        // ==========================
        // SHOW ORDER SUMMARY
        // ==========================

        StringBuilder medicineList =
                new StringBuilder();

        for (int i = 0;
             i < cart.length();
             i++) {

            try {

                JSONObject item =
                        cart.getJSONObject(i);

                String name =
                        item.optString(
                                "name",
                                "Medicine"
                        );

                int quantity =
                        item.optInt(
                                "quantity",
                                1
                        );

                medicineList
                        .append("• ")
                        .append(name)
                        .append(" × ")
                        .append(quantity);

                if (i < cart.length() - 1) {
                    medicineList.append("\n");
                }

            } catch (Exception ignored) {
            }
        }

        txtOrderMedicine.setText(
                "Medicines:\n" +
                        medicineList
        );

        txtOrderPrice.setText(
                "Medicine Total: ৳" +
                        finalMedicineTotal +
                        "\nDelivery Charge: ৳" +
                        finalDeliveryCharge +
                        "\nGrand Total: ৳" +
                        finalGrandTotal
        );

        // ==========================
        // CONFIRM ORDER
        // ==========================

        btnConfirmOrder.setOnClickListener(v -> {

            confirmOrder(
                    finalMedicineTotal,
                    finalDeliveryCharge,
                    finalGrandTotal
            );

        });

        // ==========================
        // BACK
        // ==========================

        btnBack.setOnClickListener(v ->
                finish()
        );
    }

    // ==================================================
    // CONFIRM ORDER
    // ==================================================

    private void confirmOrder(
            String finalMedicineTotal,
            String finalDeliveryCharge,
            String finalGrandTotal
    ) {

        // ==========================
        // CHECK LOGIN
        // ==========================

        FirebaseUser currentUser =
                auth.getCurrentUser();

        if (currentUser == null) {

            Toast.makeText(
                    this,
                    "Please login first.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        // ==========================
        // CHECK CART
        // ==========================

        if (cart == null ||
                cart.length() == 0) {

            Toast.makeText(
                    this,
                    "Your cart is empty.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // ==========================
        // GET CUSTOMER INFORMATION
        // ==========================

        String customerName =
                edtCustomerName
                        .getText()
                        .toString()
                        .trim();

        String phone =
                edtPhone
                        .getText()
                        .toString()
                        .trim();

        String address =
                edtAddress
                        .getText()
                        .toString()
                        .trim();

        // ==========================
        // VALIDATION
        // ==========================

        if (customerName.isEmpty()) {

            edtCustomerName.setError(
                    "Enter your name"
            );

            edtCustomerName.requestFocus();

            return;
        }

        if (phone.isEmpty()) {

            edtPhone.setError(
                    "Enter phone number"
            );

            edtPhone.requestFocus();

            return;
        }

        if (address.isEmpty()) {

            edtAddress.setError(
                    "Enter delivery address"
            );

            edtAddress.requestFocus();

            return;
        }

        // ==========================
        // DISABLE BUTTON
        // ==========================

        btnConfirmOrder.setEnabled(false);

        btnConfirmOrder.setText(
                "Placing Order..."
        );

        // ==========================
        // USER DATA
        // ==========================

        String userId =
                currentUser.getUid();

        String userEmail =
                currentUser.getEmail();

        // ==========================
        // CREATE ORDER
        // ==========================

        Map<String, Object> order =
                new HashMap<>();

        // IMPORTANT
        // Current logged-in user's UID
        order.put(
                "userId",
                userId
        );

        // User email
        order.put(
                "userEmail",
                userEmail != null
                        ? userEmail
                        : ""
        );

        // Customer name
        order.put(
                "customerName",
                customerName
        );

        // Phone
        order.put(
                "phone",
                phone
        );

        // Address
        order.put(
                "address",
                address
        );

        // Medicines
        order.put(
                "items",
                cart.toString()
        );

        // Billing details
        order.put(
                "medicineTotal",
                parseAmount(finalMedicineTotal)
        );

        order.put(
                "deliveryCharge",
                parseAmount(finalDeliveryCharge)
        );

        order.put(
                "grandTotal",
                parseAmount(finalGrandTotal)
        );

        // Initial status
        order.put(
                "status",
                "Pending"
        );

        // Order time
        order.put(
                "orderTime",
                System.currentTimeMillis()
        );

        // ==========================
        // SAVE ORDER TO FIREBASE
        // ==========================

        db.collection("orders")
                .add(order)
                .addOnSuccessListener(
                        documentReference -> {

                            // ==========================
                            // CLEAR CART
                            // ==========================

                            getSharedPreferences(
                                    "SmartPharmacyCart",
                                    MODE_PRIVATE
                            )
                                    .edit()
                                    .remove("cart")
                                    .apply();

                            // ==========================
                            // SUCCESS MESSAGE
                            // ==========================

                            Toast.makeText(
                                    OrderActivity.this,
                                    "Order placed successfully!",
                                    Toast.LENGTH_LONG
                            ).show();

                            // ==========================
                            // OPEN MY ORDERS
                            // ==========================

                            Intent intent =
                                    new Intent(
                                            OrderActivity.this,
                                            MyOrdersActivity.class
                                    );

                            intent.addFlags(
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP
                            );

                            startActivity(intent);

                            finish();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            // Enable button again
                            btnConfirmOrder.setEnabled(
                                    true
                            );

                            btnConfirmOrder.setText(
                                    "Confirm Order"
                            );

                            Toast.makeText(
                                    OrderActivity.this,
                                    "Failed to place order: " +
                                            e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }
    // ==================================================
    // PARSE AMOUNT
    // ==================================================

    private double parseAmount(String value) {

        if (value == null) {
            return 0.0;
        }

        try {
            return Double.parseDouble(
                    value.replace("৳", "").trim()
            );
        } catch (Exception e) {
            return 0.0;
        }
    }

}
