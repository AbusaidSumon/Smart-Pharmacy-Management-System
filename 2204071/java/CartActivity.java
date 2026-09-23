
        package com.example.smartpharmacy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Locale;

public class CartActivity extends AppCompatActivity {

    private LinearLayout cartContainer;
    private TextView txtGrandTotal;

    private Button btnPlaceOrder;
    private Button btnBack;

    private SharedPreferences preferences;
    private JSONArray cart;

    // ==========================
    // COLORS
    // ==========================

    private final int DARK_GREEN = Color.rgb(23, 74, 69);
    private final int GREEN = Color.rgb(40, 124, 112);
    private final int BROWN = Color.rgb(118, 85, 45);

    // Fixed delivery charge for every order
    private static final double DELIVERY_CHARGE = 60.00;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_cart);

        // ==========================
        // FIND VIEWS
        // ==========================

        cartContainer = findViewById(R.id.cartContainer);
        txtGrandTotal = findViewById(R.id.txtGrandTotal);

        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        btnBack = findViewById(R.id.btnBack);

        // ==========================
        // SHARED PREFERENCES
        // ==========================

        preferences = getSharedPreferences(
                "SmartPharmacyCart",
                MODE_PRIVATE
        );

        // ==========================
        // LOAD CART
        // ==========================

        loadCart();

        // ==========================
        // PLACE ORDER
        // ==========================

        btnPlaceOrder.setOnClickListener(v -> {

            if (cart == null || cart.length() == 0) {

                Toast.makeText(
                        CartActivity.this,
                        "Your cart is empty",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            Intent intent = new Intent(
                    CartActivity.this,
                    OrderActivity.class
            );

            intent.putExtra(
                    "cartData",
                    cart.toString()
            );

            // Send the separate billing values to OrderActivity
            double medicineTotal = calculateMedicineTotal();

            intent.putExtra(
                    "medicineTotal",
                    String.format(Locale.getDefault(), "%.2f", medicineTotal)
            );

            intent.putExtra(
                    "deliveryCharge",
                    String.format(Locale.getDefault(), "%.2f", DELIVERY_CHARGE)
            );

            intent.putExtra(
                    "grandTotal",
                    String.format(Locale.getDefault(), "%.2f", medicineTotal + DELIVERY_CHARGE)
            );

            startActivity(intent);
        });

        // ==========================
        // BACK
        // ==========================

        btnBack.setOnClickListener(v -> finish());
    }

    // ==================================================
    // LOAD CART
    // ==================================================

    private void loadCart() {

        try {

            String cartData =
                    preferences.getString(
                            "cart",
                            "[]"
                    );

            cart = new JSONArray(cartData);

        } catch (Exception e) {

            cart = new JSONArray();
        }

        displayCart();
    }

    // ==================================================
    // DISPLAY CART
    // ==================================================

    private void displayCart() {

        cartContainer.removeAllViews();

        // ==========================
        // EMPTY CART
        // ==========================

        if (cart.length() == 0) {

            TextView emptyText =
                    new TextView(this);

            emptyText.setText(
                    "Your cart is empty"
            );

            emptyText.setTextSize(20);

            emptyText.setTextColor(
                    Color.GRAY
            );

            emptyText.setGravity(
                    Gravity.CENTER
            );

            emptyText.setPadding(
                    20,
                    60,
                    20,
                    60
            );

            cartContainer.addView(
                    emptyText
            );

            txtGrandTotal.setText(
                    "Medicine Total: ৳0.00\n" +
                            "Delivery Charge: ৳0.00\n" +
                            "Grand Total: ৳0.00"
            );

            btnPlaceOrder.setEnabled(false);

            return;
        }

        btnPlaceOrder.setEnabled(true);

        double grandTotal = 0;

        // ==================================================
        // LOOP THROUGH CART
        // ==================================================

        for (int i = 0; i < cart.length(); i++) {

            try {

                JSONObject item =
                        cart.getJSONObject(i);

                String medicineName =
                        item.optString(
                                "name",
                                "Unknown Medicine"
                        );

                String priceString =
                        item.optString(
                                "price",
                                "0"
                        );

                int itemQuantity =
                        item.optInt(
                                "quantity",
                                1
                        );

                int stockLimit =
                        item.optInt(
                                "availableStock",
                                0
                        );

                // ==========================
                // PRICE
                // ==========================

                double price = 0;

                try {

                    price =
                            Double.parseDouble(
                                    priceString
                            );

                } catch (Exception ignored) {
                }

                double itemTotal =
                        price * itemQuantity;

                grandTotal += itemTotal;

                // ==================================================
                // ITEM CARD
                // ==================================================

                LinearLayout itemLayout =
                        new LinearLayout(this);

                itemLayout.setOrientation(
                        LinearLayout.VERTICAL
                );

                itemLayout.setPadding(
                        dpToPx(15),
                        dpToPx(15),
                        dpToPx(15),
                        dpToPx(15)
                );

                itemLayout.setBackgroundColor(
                        Color.WHITE
                );

                LinearLayout.LayoutParams itemParams =
                        new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );

                itemParams.setMargins(
                        0,
                        0,
                        0,
                        dpToPx(12)
                );

                itemLayout.setLayoutParams(
                        itemParams
                );

                // ==================================================
                // MEDICINE NAME
                // ==================================================

                TextView nameText =
                        new TextView(this);

                nameText.setText(
                        "💊 " + medicineName
                );

                nameText.setTextSize(21);

                nameText.setTextColor(
                        DARK_GREEN
                );

                nameText.setTypeface(
                        null,
                        Typeface.BOLD
                );

                // ==================================================
                // PRICE
                // ==================================================

                TextView priceText =
                        new TextView(this);

                priceText.setText(
                        "Price: ৳" +
                                String.format(
                                        Locale.getDefault(),
                                        "%.2f",
                                        price
                                )
                );

                priceText.setTextSize(17);

                priceText.setTextColor(
                        Color.DKGRAY
                );

                priceText.setPadding(
                        0,
                        dpToPx(7),
                        0,
                        dpToPx(5)
                );

                // ==================================================
                // QUANTITY ROW
                // ==================================================

                LinearLayout quantityRow =
                        new LinearLayout(this);

                quantityRow.setOrientation(
                        LinearLayout.HORIZONTAL
                );

                quantityRow.setGravity(
                        Gravity.CENTER_VERTICAL
                );

                quantityRow.setPadding(
                        0,
                        dpToPx(8),
                        0,
                        dpToPx(8)
                );

                // ==================================================
                // QUANTITY LABEL
                // ==================================================

                TextView quantityLabel =
                        new TextView(this);

                quantityLabel.setText(
                        "Quantity:"
                );

                quantityLabel.setTextSize(18);

                quantityLabel.setTextColor(
                        DARK_GREEN
                );

                quantityLabel.setTypeface(
                        null,
                        Typeface.BOLD
                );

                LinearLayout.LayoutParams labelParams =
                        new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                dpToPx(50)
                        );

                quantityLabel.setLayoutParams(
                        labelParams
                );

                // ==================================================
                // MINUS BUTTON
                // ==================================================

                TextView minusButton =
                        createQuantityControl(
                                "−",
                                GREEN,
                                Color.WHITE
                        );

                LinearLayout.LayoutParams minusParams =
                        new LinearLayout.LayoutParams(
                                dpToPx(55),
                                dpToPx(50)
                        );

                minusParams.setMargins(
                        dpToPx(10),
                        0,
                        dpToPx(5),
                        0
                );

                minusButton.setLayoutParams(
                        minusParams
                );

                // ==================================================
                // QUANTITY DISPLAY
                // ==================================================

                TextView quantityButton =
                        createQuantityControl(
                                String.valueOf(itemQuantity),
                                Color.WHITE,
                                DARK_GREEN
                        );

                quantityButton.setTextSize(20);

                quantityButton.setTypeface(
                        null,
                        Typeface.BOLD
                );

                LinearLayout.LayoutParams quantityParams =
                        new LinearLayout.LayoutParams(
                                dpToPx(90),
                                dpToPx(50)
                        );

                quantityParams.setMargins(
                        dpToPx(3),
                        0,
                        dpToPx(3),
                        0
                );

                quantityButton.setLayoutParams(
                        quantityParams
                );

                // ==================================================
                // PLUS BUTTON
                // ==================================================

                TextView plusButton =
                        createQuantityControl(
                                "+",
                                GREEN,
                                Color.WHITE
                        );

                LinearLayout.LayoutParams plusParams =
                        new LinearLayout.LayoutParams(
                                dpToPx(55),
                                dpToPx(50)
                        );

                plusParams.setMargins(
                        dpToPx(5),
                        0,
                        0,
                        0
                );

                plusButton.setLayoutParams(
                        plusParams
                );

                // ==================================================
                // ADD QUANTITY CONTROLS
                // ==================================================

                quantityRow.addView(
                        quantityLabel
                );

                quantityRow.addView(
                        minusButton
                );

                quantityRow.addView(
                        quantityButton
                );

                quantityRow.addView(
                        plusButton
                );

                // ==================================================
                // STOCK
                // ==================================================

                TextView stockText =
                        new TextView(this);

                stockText.setText(
                        "Available Stock: " +
                                stockLimit
                );

                stockText.setTextSize(16);

                stockText.setTextColor(
                        Color.DKGRAY
                );

                stockText.setPadding(
                        0,
                        dpToPx(3),
                        0,
                        0
                );

                // ==================================================
                // TOTAL
                // ==================================================

                TextView totalText =
                        new TextView(this);

                totalText.setText(
                        "Total: ৳" +
                                String.format(
                                        Locale.getDefault(),
                                        "%.2f",
                                        itemTotal
                                )
                );

                totalText.setTextSize(18);

                totalText.setTextColor(
                        BROWN
                );

                totalText.setTypeface(
                        null,
                        Typeface.BOLD
                );

                totalText.setPadding(
                        0,
                        dpToPx(7),
                        0,
                        dpToPx(12)
                );

                // ==================================================
                // REMOVE
                // ==================================================

                Button removeButton =
                        new Button(this);

                removeButton.setText(
                        "REMOVE"
                );

                removeButton.setTextSize(14);

                // ==================================================
                // ADD TO CARD
                // ==================================================

                itemLayout.addView(
                        nameText
                );

                itemLayout.addView(
                        priceText
                );

                itemLayout.addView(
                        quantityRow
                );

                itemLayout.addView(
                        stockText
                );

                itemLayout.addView(
                        totalText
                );

                itemLayout.addView(
                        removeButton
                );

                cartContainer.addView(
                        itemLayout
                );

                // ==================================================
                // POSITION
                // ==================================================

                final int position = i;

                // ==================================================
                // CLICK QUANTITY NUMBER
                // ==================================================

                quantityButton.setOnClickListener(v -> {

                    showQuantityDialog(
                            position,
                            medicineName
                    );
                });

                // ==================================================
                // MINUS
                // ==================================================

                minusButton.setOnClickListener(v -> {

                    try {

                        JSONObject selectedItem =
                                cart.getJSONObject(
                                        position
                                );

                        int currentQuantity =
                                selectedItem.optInt(
                                        "quantity",
                                        1
                                );

                        if (currentQuantity <= 1) {

                            Toast.makeText(
                                    CartActivity.this,
                                    "Minimum quantity is 1",
                                    Toast.LENGTH_SHORT
                            ).show();

                            return;
                        }

                        selectedItem.put(
                                "quantity",
                                currentQuantity - 1
                        );

                        saveCart();

                        displayCart();

                    } catch (Exception e) {

                        Toast.makeText(
                                CartActivity.this,
                                "Unable to decrease quantity",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });

                // ==================================================
                // PLUS
                // ==================================================

                plusButton.setOnClickListener(v -> {

                    try {

                        JSONObject selectedItem =
                                cart.getJSONObject(
                                        position
                                );

                        int currentQuantity =
                                selectedItem.optInt(
                                        "quantity",
                                        1
                                );

                        int currentStock =
                                selectedItem.optInt(
                                        "availableStock",
                                        0
                                );

                        if (currentStock <= 0) {

                            Toast.makeText(
                                    CartActivity.this,
                                    "Medicine is out of stock",
                                    Toast.LENGTH_SHORT
                            ).show();

                            return;
                        }

                        if (currentQuantity >=
                                currentStock) {

                            Toast.makeText(
                                    CartActivity.this,
                                    "Cannot exceed available stock",
                                    Toast.LENGTH_SHORT
                            ).show();

                            return;
                        }

                        selectedItem.put(
                                "quantity",
                                currentQuantity + 1
                        );

                        saveCart();

                        displayCart();

                    } catch (Exception e) {

                        Toast.makeText(
                                CartActivity.this,
                                "Unable to increase quantity",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });

                // ==================================================
                // REMOVE
                // ==================================================

                removeButton.setOnClickListener(v -> {

                    cart.remove(position);

                    saveCart();

                    displayCart();

                    Toast.makeText(
                            CartActivity.this,
                            medicineName +
                                    " removed from cart",
                            Toast.LENGTH_SHORT
                    ).show();
                });

            } catch (Exception e) {

                e.printStackTrace();
            }
        }

        // ==================================================
        // GRAND TOTAL
        // ==================================================

        double finalGrandTotal = grandTotal + DELIVERY_CHARGE;

        txtGrandTotal.setText(
                "Medicine Total: ৳" +
                        String.format(
                                Locale.getDefault(),
                                "%.2f",
                                grandTotal
                        ) +
                        "\nDelivery Charge: ৳" +
                        String.format(
                                Locale.getDefault(),
                                "%.2f",
                                DELIVERY_CHARGE
                        ) +
                        "\nGrand Total: ৳" +
                        String.format(
                                Locale.getDefault(),
                                "%.2f",
                                finalGrandTotal
                        )
        );
    }

    // ==================================================
    // CREATE QUANTITY CONTROL
    // ==================================================

    private TextView createQuantityControl(
            String text,
            int backgroundColor,
            int textColor
    ) {

        TextView view =
                new TextView(this);

        view.setText(
                text
        );

        view.setTextSize(
                20
        );

        view.setTextColor(
                textColor
        );

        view.setGravity(
                Gravity.CENTER
        );

        view.setTypeface(
                null,
                Typeface.BOLD
        );

        GradientDrawable background =
                new GradientDrawable();

        background.setColor(
                backgroundColor
        );

        background.setCornerRadius(
                dpToPx(8)
        );

        background.setStroke(
                dpToPx(1),
                GREEN
        );

        view.setBackground(
                background
        );

        view.setClickable(true);

        view.setFocusable(true);

        return view;
    }

    // ==================================================
    // QUANTITY DIALOG
    // ==================================================

    private void showQuantityDialog(
            int position,
            String medicineName
    ) {

        try {

            JSONObject selectedItem =
                    cart.getJSONObject(
                            position
                    );

            int currentQuantity =
                    selectedItem.optInt(
                            "quantity",
                            1
                    );

            int stockLimit =
                    selectedItem.optInt(
                            "availableStock",
                            0
                    );

            // ==================================================
            // CUSTOM DIALOG VIEW
            // ==================================================

            LinearLayout dialogLayout =
                    new LinearLayout(this);

            dialogLayout.setOrientation(
                    LinearLayout.VERTICAL
            );

            dialogLayout.setPadding(
                    dpToPx(25),
                    dpToPx(5),
                    dpToPx(25),
                    dpToPx(5)
            );

            // ==================================================
            // MEDICINE
            // ==================================================

            TextView medicineText =
                    new TextView(this);

            medicineText.setText(
                    medicineName
            );

            medicineText.setTextSize(
                    20
            );

            medicineText.setTextColor(
                    DARK_GREEN
            );

            medicineText.setTypeface(
                    null,
                    Typeface.BOLD
            );

            // ==================================================
            // STOCK
            // ==================================================

            TextView stockText =
                    new TextView(this);

            stockText.setText(
                    "Available Stock: " +
                            stockLimit
            );

            stockText.setTextSize(
                    17
            );

            stockText.setTextColor(
                    Color.DKGRAY
            );

            stockText.setPadding(
                    0,
                    dpToPx(8),
                    0,
                    dpToPx(15)
            );

            // ==================================================
            // QUANTITY LABEL
            // ==================================================

            TextView quantityTitle =
                    new TextView(this);

            quantityTitle.setText(
                    "Enter Quantity"
            );

            quantityTitle.setTextSize(
                    16
            );

            quantityTitle.setTextColor(
                    DARK_GREEN
            );

            quantityTitle.setTypeface(
                    null,
                    Typeface.BOLD
            );

            // ==================================================
            // QUANTITY INPUT
            // ==================================================

            EditText quantityInput =
                    new EditText(this);

            quantityInput.setInputType(
                    InputType.TYPE_CLASS_NUMBER
            );

            quantityInput.setSingleLine(
                    true
            );

            quantityInput.setText(
                    String.valueOf(
                            currentQuantity
                    )
            );

            quantityInput.setTextSize(
                    24
            );

            quantityInput.setTextColor(
                    Color.BLACK
            );

            quantityInput.setGravity(
                    Gravity.CENTER
            );

            quantityInput.setSelectAllOnFocus(
                    true
            );

            quantityInput.setPadding(
                    dpToPx(10),
                    0,
                    dpToPx(10),
                    0
            );

            LinearLayout.LayoutParams
                    inputParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            dpToPx(65)
                    );

            inputParams.setMargins(
                    0,
                    dpToPx(8),
                    0,
                    dpToPx(8)
            );

            quantityInput.setLayoutParams(
                    inputParams
            );

            // ==================================================
            // ADD TO DIALOG
            // ==================================================

            dialogLayout.addView(
                    medicineText
            );

            dialogLayout.addView(
                    stockText
            );

            dialogLayout.addView(
                    quantityTitle
            );

            dialogLayout.addView(
                    quantityInput
            );

            // ==================================================
            // DIALOG
            // ==================================================

            AlertDialog dialog =
                    new AlertDialog.Builder(this)
                            .setTitle(
                                    "Set Quantity"
                            )
                            .setView(
                                    dialogLayout
                            )
                            .setNegativeButton(
                                    "CANCEL",
                                    null
                            )
                            .setPositiveButton(
                                    "UPDATE",
                                    null
                            )
                            .create();

            dialog.show();

            // ==================================================
            // DIALOG WIDTH
            // ==================================================

            if (dialog.getWindow() != null) {

                dialog.getWindow().setLayout(
                        (int) (
                                getResources()
                                        .getDisplayMetrics()
                                        .widthPixels * 0.90
                        ),
                        WindowManager.LayoutParams.WRAP_CONTENT
                );
            }

            // ==================================================
            // UPDATE BUTTON
            // ==================================================

            Button updateButton =
                    dialog.getButton(
                            AlertDialog.BUTTON_POSITIVE
                    );

            updateButton.setOnClickListener(v -> {

                String value =
                        quantityInput
                                .getText()
                                .toString()
                                .trim();

                // ==========================
                // EMPTY
                // ==========================

                if (value.isEmpty()) {

                    quantityInput.setError(
                            "Enter quantity"
                    );

                    return;
                }

                try {

                    int newQuantity =
                            Integer.parseInt(
                                    value
                            );

                    // ==========================
                    // MINIMUM
                    // ==========================

                    if (newQuantity <= 0) {

                        quantityInput.setError(
                                "Quantity must be at least 1"
                        );

                        return;
                    }

                    // ==========================
                    // STOCK
                    // ==========================

                    if (stockLimit <= 0) {

                        Toast.makeText(
                                CartActivity.this,
                                "Medicine is out of stock",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    if (newQuantity >
                            stockLimit) {

                        quantityInput.setError(
                                "Maximum available: " +
                                        stockLimit
                        );

                        return;
                    }

                    // ==========================
                    // UPDATE CART
                    // ==========================

                    selectedItem.put(
                            "quantity",
                            newQuantity
                    );

                    saveCart();

                    dialog.dismiss();

                    displayCart();

                    Toast.makeText(
                            CartActivity.this,
                            "Quantity updated to " +
                                    newQuantity,
                            Toast.LENGTH_SHORT
                    ).show();

                } catch (NumberFormatException e) {

                    quantityInput.setError(
                            "Enter a valid number"
                    );

                } catch (Exception e) {

                    Toast.makeText(
                            CartActivity.this,
                            "Unable to update quantity",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            });

            // ==================================================
            // OPEN KEYBOARD
            // ==================================================

            quantityInput.requestFocus();

            if (dialog.getWindow() != null) {

                dialog.getWindow().setSoftInputMode(
                        WindowManager.LayoutParams
                                .SOFT_INPUT_STATE_ALWAYS_VISIBLE
                );
            }

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Unable to set quantity",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // ==================================================
    // SAVE CART
    // ==================================================

    private void saveCart() {

        preferences.edit()
                .putString(
                        "cart",
                        cart.toString()
                )
                .apply();
    }

    // ==================================================
    // CALCULATE MEDICINE TOTAL
    // ==================================================

    private double calculateMedicineTotal() {

        double total = 0;

        if (cart == null) {
            return 0;
        }

        for (int i = 0; i < cart.length(); i++) {

            try {
                JSONObject item = cart.getJSONObject(i);

                double price = Double.parseDouble(
                        item.optString("price", "0")
                );

                int quantity = item.optInt("quantity", 1);

                total += price * quantity;

            } catch (Exception ignored) {
            }
        }

        return total;
    }

    // ==================================================
    // DP TO PX
    // ==================================================

    private int dpToPx(int dp) {

        return Math.round(
                dp *
                        getResources()
                                .getDisplayMetrics()
                                .density
        );
    }
}
