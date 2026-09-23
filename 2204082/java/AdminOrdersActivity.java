

package com.example.smartpharmacy;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminOrdersActivity extends AppCompatActivity {

    private LinearLayout orderContainer;
    private Button btnRefresh;
    private Button btnBack;

    private FirebaseFirestore db;

    private static final int DARK_GREEN = Color.rgb(23, 74, 69);
    private static final int GREEN = Color.rgb(40, 124, 112);
    private static final int BLUE = Color.rgb(52, 101, 164);
    private static final int BROWN = Color.rgb(118, 85, 45);
    private static final int RED = Color.rgb(190, 60, 60);
    private static final int ORANGE = Color.rgb(225, 132, 45);
    private static final int DARK_TEXT = Color.rgb(65, 65, 65);
    private static final int GRAY = Color.rgb(120, 120, 120);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_orders);

        db = FirebaseFirestore.getInstance();

        orderContainer = findViewById(R.id.orderContainer);
        btnRefresh = findViewById(R.id.btnRefresh);
        btnBack = findViewById(R.id.btnBack);

        loadOrders();

        btnRefresh.setOnClickListener(v -> loadOrders());

        btnBack.setOnClickListener(v -> finish());
    }

    // ==================================================
    // LOAD ORDERS
    // ==================================================

    private void loadOrders() {

        orderContainer.removeAllViews();

        showMessage("Loading customer orders...");

        db.collection("orders")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    orderContainer.removeAllViews();

                    if (queryDocumentSnapshots.isEmpty()) {
                        showMessage("No customer orders found.");
                        return;
                    }

                    List<DocumentSnapshot> orderList =
                            new ArrayList<>(
                                    queryDocumentSnapshots.getDocuments()
                            );

                    orderList.sort(
                            (first, second) ->
                                    Long.compare(
                                            getOrderTime(second),
                                            getOrderTime(first)
                                    )
                    );

                    for (DocumentSnapshot document : orderList) {
                        addOrderCard(document);
                    }

                })
                .addOnFailureListener(e -> {

                    orderContainer.removeAllViews();

                    showMessage("Unable to load customer orders.");

                    Toast.makeText(
                            AdminOrdersActivity.this,
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    // ==================================================
    // ORDER TIME
    // ==================================================

    private long getOrderTime(DocumentSnapshot document) {

        Object timeObject = document.get("orderTime");

        if (timeObject instanceof Number) {
            return ((Number) timeObject).longValue();
        }

        return 0;
    }

    // ==================================================
    // ADD ORDER CARD
    // ==================================================

    private void addOrderCard(DocumentSnapshot document) {

        String orderId = document.getId();

        String customerName =
                document.getString("customerName");

        String phone =
                document.getString("phone");

        String address =
                document.getString("address");

        String status =
                document.getString("status");

        if (customerName == null || customerName.trim().isEmpty()) {
            customerName = "N/A";
        }

        if (phone == null || phone.trim().isEmpty()) {
            phone = "N/A";
        }

        if (address == null || address.trim().isEmpty()) {
            address = "N/A";
        }

        if (status == null || status.trim().isEmpty()) {
            status = "Pending";
        }

        Object medicineTotalObject = document.get("medicineTotal");
        Object deliveryChargeObject = document.get("deliveryCharge");
        Object totalObject = document.get("grandTotal");
        Object timeObject = document.get("orderTime");

        String medicineTotalText = getTotalText(medicineTotalObject);
        String deliveryChargeText = getTotalText(deliveryChargeObject);
        String totalText = getTotalText(totalObject);
        String orderDate = getOrderDate(timeObject);

        // ==================================================
        // CARD
        // ==================================================

        LinearLayout card = new LinearLayout(this);

        card.setOrientation(LinearLayout.VERTICAL);

        card.setPadding(
                20,
                20,
                20,
                18
        );

        GradientDrawable cardBackground =
                new GradientDrawable();

        cardBackground.setColor(Color.WHITE);
        cardBackground.setCornerRadius(14);

        card.setBackground(cardBackground);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(
                0,
                0,
                0,
                18
        );

        card.setLayoutParams(cardParams);

        // ==================================================
        // ORDER ID
        // ==================================================

        TextView txtOrderId =
                createTextView(
                        "📦 Order ID:\n" + orderId,
                        17,
                        DARK_GREEN
                );

        txtOrderId.setTypeface(null, Typeface.BOLD);

        card.addView(txtOrderId);

        // ==================================================
        // CUSTOMER
        // ==================================================

        card.addView(
                createTextView(
                        "👤 Customer: " + customerName,
                        16,
                        DARK_TEXT
                )
        );

        // ==================================================
        // PHONE
        // ==================================================

        card.addView(
                createTextView(
                        "📞 Phone: " + phone,
                        16,
                        DARK_TEXT
                )
        );

        // ==================================================
        // ADDRESS
        // ==================================================

        card.addView(
                createTextView(
                        "📍 Address: " + address,
                        16,
                        DARK_TEXT
                )
        );

        // ==================================================
        // MEDICINES
        // ==================================================

        TextView medicineTitle =
                createTextView(
                        "💊 Medicines:",
                        17,
                        DARK_GREEN
                );

        medicineTitle.setTypeface(
                null,
                Typeface.BOLD
        );

        medicineTitle.setPadding(
                0,
                12,
                0,
                5
        );

        card.addView(medicineTitle);

        addMedicineItems(
                card,
                document.get("items")
        );

        // ==================================================
        // TOTAL
        // ==================================================

        TextView txtTotal =
                createTextView(
                        "💵 Medicine Total: " + medicineTotalText +
                                "\n🚚 Delivery Charge: " + deliveryChargeText +
                                "\n💰 Grand Total: " + totalText,
                        18,
                        BROWN
                );

        txtTotal.setTypeface(
                null,
                Typeface.BOLD
        );

        txtTotal.setPadding(
                0,
                12,
                0,
                5
        );

        card.addView(txtTotal);

        // ==================================================
        // STATUS
        // ==================================================

        TextView txtStatus =
                createTextView(
                        "Status: " + getStatusIcon(status)
                                + " " + status,
                        17,
                        getStatusColor(status)
                );

        txtStatus.setTypeface(
                null,
                Typeface.BOLD
        );

        txtStatus.setPadding(
                0,
                10,
                0,
                5
        );

        card.addView(txtStatus);

        // ==================================================
        // DATE
        // ==================================================

        card.addView(
                createTextView(
                        "🕒 " + orderDate,
                        14,
                        GRAY
                )
        );

        // ==================================================
        // ACTIONS
        // ==================================================

        if (status.equalsIgnoreCase("Pending")) {

            LinearLayout buttonRow =
                    new LinearLayout(this);

            buttonRow.setOrientation(
                    LinearLayout.HORIZONTAL
            );

            buttonRow.setGravity(Gravity.CENTER);

            LinearLayout.LayoutParams rowParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            55
                    );

            rowParams.setMargins(
                    0,
                    15,
                    0,
                    5
            );

            buttonRow.setLayoutParams(rowParams);

            // CONFIRM

            Button btnConfirm =
                    createActionButton(
                            "✓ Confirm",
                            GREEN
                    );

            LinearLayout.LayoutParams confirmParams =
                    new LinearLayout.LayoutParams(
                            0,
                            55,
                            1
                    );

            confirmParams.setMargins(
                    0,
                    0,
                    6,
                    0
            );

            btnConfirm.setLayoutParams(confirmParams);

            btnConfirm.setOnClickListener(v -> {

                btnConfirm.setEnabled(false);

                updateOrderStatus(
                        orderId,
                        "Confirmed"
                );
            });

            // CANCEL

            Button btnCancel =
                    createActionButton(
                            "✕ Cancel",
                            RED
                    );

            LinearLayout.LayoutParams cancelParams =
                    new LinearLayout.LayoutParams(
                            0,
                            55,
                            1
                    );

            cancelParams.setMargins(
                    6,
                    0,
                    0,
                    0
            );

            btnCancel.setLayoutParams(cancelParams);

            btnCancel.setOnClickListener(v -> {

                btnCancel.setEnabled(false);

                updateOrderStatus(
                        orderId,
                        "Cancelled"
                );
            });

            buttonRow.addView(btnConfirm);
            buttonRow.addView(btnCancel);

            card.addView(buttonRow);
        }

        // ==================================================
        // STATUS UPDATE BUTTON
        // ==================================================

        if (!status.equalsIgnoreCase("Pending")
                && !status.equalsIgnoreCase("Cancelled")
                && !status.equalsIgnoreCase("Delivered")) {

            Button btnUpdateStatus =
                    createActionButton(
                            "🔄 Update Order Status",
                            BLUE
                    );

            LinearLayout.LayoutParams statusParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            55
                    );

            statusParams.setMargins(
                    0,
                    15,
                    0,
                    5
            );

            btnUpdateStatus.setLayoutParams(
                    statusParams
            );
            final String currentStatus = status;
            btnUpdateStatus.setOnClickListener(
                    v -> showStatusDialog(
                            orderId,
                            currentStatus
                    )
            );

            card.addView(btnUpdateStatus);
        }

        // ==================================================
        // DELIVERED MESSAGE
        // ==================================================

        if (status.equalsIgnoreCase("Delivered")) {

            TextView delivered =
                    createTextView(
                            "✅ Order delivered successfully",
                            15,
                            GREEN
                    );

            delivered.setTypeface(
                    null,
                    Typeface.BOLD
            );

            delivered.setGravity(
                    Gravity.CENTER
            );

            delivered.setPadding(
                    0,
                    15,
                    0,
                    5
            );

            card.addView(delivered);
        }

        // ==================================================
        // ADD CARD
        // ==================================================

        orderContainer.addView(card);
    }

    // ==================================================
    // STATUS DIALOG
    // ==================================================

    private void showStatusDialog(
            String orderId,
            String currentStatus
    ) {

        String[] statuses = {
                "Processing",
                "Parcel Ready",
                "Out for Delivery",
                "Delivered"
        };

        AlertDialog.Builder builder =
                new AlertDialog.Builder(this);

        builder.setTitle("Update Order Status");

        builder.setItems(
                statuses,
                (dialog, which) -> {

                    String selectedStatus =
                            statuses[which];

                    if (selectedStatus.equalsIgnoreCase(
                            currentStatus
                    )) {

                        Toast.makeText(
                                AdminOrdersActivity.this,
                                "This status is already selected.",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    updateOrderStatus(
                            orderId,
                            selectedStatus
                    );
                }
        );

        builder.setNegativeButton(
                "CANCEL",
                null
        );

        builder.show();
    }

    // ==================================================
    // UPDATE STATUS + CREATE NOTIFICATION
    // ==================================================

    private void updateOrderStatus(
            String orderId,
            String newStatus
    ) {

        // ==================================================
        // CONFIRM ORDER
        // Confirm করলে medicine stock কমবে
        // ==================================================

        if (newStatus.equalsIgnoreCase("Confirmed")) {

            confirmOrderAndReduceStock(orderId);

            return;
        }

        // ==================================================
        // OTHER STATUS
        // Processing / Parcel Ready /
        // Out for Delivery / Delivered / Cancelled
        // ==================================================

        db.collection("orders")
                .document(orderId)
                .get()
                .addOnSuccessListener(orderDocument -> {

                    if (!orderDocument.exists()) {

                        Toast.makeText(
                                AdminOrdersActivity.this,
                                "Order not found.",
                                Toast.LENGTH_SHORT
                        ).show();

                        loadOrders();

                        return;
                    }

                    String userId =
                            orderDocument.getString("userId");

                    db.collection("orders")
                            .document(orderId)
                            .update(
                                    "status",
                                    newStatus
                            )
                            .addOnSuccessListener(unused -> {

                                // ==================================================
                                // CREATE NOTIFICATION
                                // ==================================================

                                if (userId != null
                                        && !userId.trim().isEmpty()) {

                                    createNotification(
                                            userId,
                                            orderId,
                                            newStatus
                                    );
                                }

                                Toast.makeText(
                                        AdminOrdersActivity.this,
                                        "Order " +
                                                newStatus.toLowerCase(
                                                        Locale.getDefault()
                                                ) +
                                                " successfully",
                                        Toast.LENGTH_SHORT
                                ).show();

                                loadOrders();

                            })
                            .addOnFailureListener(e -> {

                                Toast.makeText(
                                        AdminOrdersActivity.this,
                                        "Failed to update order: "
                                                + e.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();

                                loadOrders();
                            });
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            AdminOrdersActivity.this,
                            "Unable to find order: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();

                    loadOrders();
                });
    }

    // ==================================================
    // CONFIRM ORDER + REDUCE STOCK
    // ==================================================

    private void confirmOrderAndReduceStock(
            String orderId
    ) {

        db.runTransaction(transaction -> {

            // ==================================================
            // GET ORDER
            // ==================================================

            com.google.firebase.firestore.DocumentReference orderRef =
                    db.collection("orders")
                            .document(orderId);

            DocumentSnapshot orderDocument =
                    transaction.get(orderRef);

            if (!orderDocument.exists()) {

                throw new RuntimeException(
                        "Order not found."
                );
            }

            // ==================================================
            // CHECK CURRENT STATUS
            // ==================================================

            String currentStatus =
                    orderDocument.getString("status");

            if (currentStatus == null) {
                currentStatus = "Pending";
            }

            // Already confirmed/processed হলে
            // আবার stock কমবে না
            if (!currentStatus.equalsIgnoreCase("Pending")) {

                throw new RuntimeException(
                        "This order is already " +
                                currentStatus + "."
                );
            }

            // ==================================================
            // GET USER ID
            // ==================================================

            String userId =
                    orderDocument.getString("userId");

            if (userId == null ||
                    userId.trim().isEmpty()) {

                throw new RuntimeException(
                        "User information not found."
                );
            }

            // ==================================================
            // GET ORDER ITEMS
            // ==================================================

            Object itemsObject =
                    orderDocument.get("items");

            if (itemsObject == null) {

                throw new RuntimeException(
                        "No medicine found in this order."
                );
            }

            // medicineId -> total ordered quantity
            java.util.Map<String, Integer>
                    medicineQuantities =
                    new java.util.HashMap<>();

            // ==================================================
            // ITEMS SAVED AS JSON STRING
            // ==================================================

            if (itemsObject instanceof String) {

                String itemsString =
                        (String) itemsObject;

                try {

                    JSONArray items =
                            new JSONArray(itemsString);

                    for (
                            int i = 0;
                            i < items.length();
                            i++
                    ) {

                        JSONObject item =
                                items.getJSONObject(i);

                        String medicineId =
                                item.optString(
                                        "medicineId",
                                        ""
                                );

                        // Old cart-এর জন্য name fallback
                        if (medicineId.trim().isEmpty()) {

                            medicineId =
                                    item.optString(
                                            "name",
                                            ""
                                    );
                        }

                        if (medicineId.trim().isEmpty()) {

                            throw new RuntimeException(
                                    "Medicine ID not found."
                            );
                        }

                        int quantity =
                                item.optInt(
                                        "quantity",
                                        0
                                );

                        if (quantity <= 0) {

                            throw new RuntimeException(
                                    "Invalid medicine quantity."
                            );
                        }

                        int oldQuantity =
                                medicineQuantities.containsKey(
                                        medicineId
                                )
                                        ? medicineQuantities.get(
                                        medicineId
                                )
                                        : 0;

                        medicineQuantities.put(
                                medicineId,
                                oldQuantity + quantity
                        );
                    }

                } catch (Exception e) {

                    if (e instanceof RuntimeException) {
                        throw (RuntimeException) e;
                    }

                    throw new RuntimeException(
                            "Unable to read order medicines."
                    );
                }

            } else if (itemsObject instanceof List) {

                // ==================================================
                // FIRESTORE LIST FALLBACK
                // ==================================================

                List<?> items =
                        (List<?>) itemsObject;

                for (Object object : items) {

                    if (!(object instanceof Map)) {
                        continue;
                    }

                    Map<?, ?> item =
                            (Map<?, ?>) object;

                    Object medicineIdObject =
                            item.get("medicineId");

                    if (medicineIdObject == null) {

                        medicineIdObject =
                                item.get("name");
                    }

                    if (medicineIdObject == null) {

                        throw new RuntimeException(
                                "Medicine ID not found."
                        );
                    }

                    String medicineId =
                            String.valueOf(
                                    medicineIdObject
                            );

                    Object quantityObject =
                            item.get("quantity");

                    int quantity = 0;

                    if (quantityObject instanceof Number) {

                        quantity =
                                ((Number)
                                        quantityObject)
                                        .intValue();

                    } else if (quantityObject != null) {

                        try {

                            quantity =
                                    Integer.parseInt(
                                            String.valueOf(
                                                    quantityObject
                                            )
                                    );

                        } catch (Exception ignored) {
                        }
                    }

                    if (quantity <= 0) {

                        throw new RuntimeException(
                                "Invalid medicine quantity."
                        );
                    }

                    int oldQuantity =
                            medicineQuantities.containsKey(
                                    medicineId
                            )
                                    ? medicineQuantities.get(
                                    medicineId
                            )
                                    : 0;

                    medicineQuantities.put(
                            medicineId,
                            oldQuantity + quantity
                    );
                }
            }

            // ==================================================
            // CHECK ITEMS
            // ==================================================

            if (medicineQuantities.isEmpty()) {

                throw new RuntimeException(
                        "No medicine found in this order."
                );
            }

            // ==================================================
            // STORE REFERENCES AND NEW STOCK
            // ==================================================

            java.util.Map<
                    String,
                    com.google.firebase.firestore.DocumentReference
                    > medicineRefs =
                    new java.util.HashMap<>();

            java.util.Map<
                    String,
                    Integer
                    > newStocks =
                    new java.util.HashMap<>();

            // ==================================================
            // READ AND CHECK ALL MEDICINE STOCK
            // ==================================================

            for (
                    String medicineId :
                    medicineQuantities.keySet()
            ) {

                com.google.firebase.firestore.DocumentReference
                        medicineRef =
                        db.collection("medicines")
                                .document(medicineId);

                DocumentSnapshot medicineDocument =
                        transaction.get(medicineRef);

                if (!medicineDocument.exists()) {

                    throw new RuntimeException(
                            "Medicine not found: " +
                                    medicineId
                    );
                }

                Object stockObject =
                        medicineDocument.get(
                                "quantity"
                        );

                int currentStock = 0;

                if (stockObject instanceof Number) {

                    currentStock =
                            ((Number) stockObject)
                                    .intValue();

                } else if (stockObject != null) {

                    try {

                        currentStock =
                                Integer.parseInt(
                                        String.valueOf(
                                                stockObject
                                        )
                                );

                    } catch (Exception ignored) {
                    }
                }

                int orderQuantity =
                        medicineQuantities.get(
                                medicineId
                        );

                // ==================================================
                // NOT ENOUGH STOCK
                // ==================================================

                if (currentStock < orderQuantity) {

                    throw new RuntimeException(
                            "Not enough stock for " +
                                    medicineId +
                                    ". Available: " +
                                    currentStock +
                                    ", Required: " +
                                    orderQuantity
                    );
                }

                int newStock =
                        currentStock - orderQuantity;

                medicineRefs.put(
                        medicineId,
                        medicineRef
                );

                newStocks.put(
                        medicineId,
                        newStock
                );
            }

            // ==================================================
            // UPDATE MEDICINE STOCK
            // ==================================================

            for (
                    String medicineId :
                    medicineRefs.keySet()
            ) {

                transaction.update(
                        medicineRefs.get(
                                medicineId
                        ),
                        "quantity",
                        newStocks.get(
                                medicineId
                        )
                );
            }

            // ==================================================
            // UPDATE ORDER STATUS
            // ==================================================

            transaction.update(
                    orderRef,
                    "status",
                    "Confirmed"
            );

            // Return userId after successful transaction
            return userId;

        }).addOnSuccessListener(userIdObject -> {

            String userId =
                    String.valueOf(
                            userIdObject
                    );

            // ==================================================
            // CREATE CONFIRMATION NOTIFICATION
            // ==================================================

            createNotification(
                    userId,
                    orderId,
                    "Confirmed"
            );

            Toast.makeText(
                    AdminOrdersActivity.this,
                    "Order confirmed and stock updated successfully.",
                    Toast.LENGTH_LONG
            ).show();

            loadOrders();

        }).addOnFailureListener(e -> {

            Toast.makeText(
                    AdminOrdersActivity.this,
                    "Order confirmation failed: " +
                            e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();

            loadOrders();
        });
    }

    // ==================================================
    // CREATE NOTIFICATION
    // ==================================================

    private void createNotification(
            String userId,
            String orderId,
            String status
    ) {

        String title;
        String message;

        switch (status) {

            case "Confirmed":

                title = "Order Confirmed ✅";

                message =
                        "Your order has been confirmed successfully.";

                break;

            case "Cancelled":

                title = "Order Cancelled ❌";

                message =
                        "Your order has been cancelled.";

                break;

            case "Processing":

                title = "Order Processing 🔵";

                message =
                        "Your order is now being processed.";

                break;

            case "Parcel Ready":

                title = "Parcel Ready 📦";

                message =
                        "Your parcel is ready for delivery.";

                break;

            case "Out for Delivery":

                title = "Out for Delivery 🚚";

                message =
                        "Your order is on the way.";

                break;

            case "Delivered":

                title = "Order Delivered ✅";

                message =
                        "Your order has been delivered successfully.";

                break;

            default:

                title = "Order Status Updated 🔔";

                message =
                        "Your order status is now " + status + ".";
        }

        Map<String, Object> notification =
                new java.util.HashMap<>();

        notification.put(
                "userId",
                userId
        );

        notification.put(
                "title",
                title
        );

        notification.put(
                "message",
                message
        );

        notification.put(
                "orderId",
                orderId
        );

        notification.put(
                "status",
                status
        );

        notification.put(
                "timestamp",
                System.currentTimeMillis()
        );

        notification.put(
                "read",
                false
        );

        db.collection("notifications")
                .add(notification);
    }

    // ==================================================
    // ACTION BUTTON
    // ==================================================

    private Button createActionButton(
            String text,
            int backgroundColor
    ) {

        Button button = new Button(this);

        button.setText(text);
        button.setTextSize(16);
        button.setTextColor(Color.WHITE);
        button.setGravity(Gravity.CENTER);
        button.setTypeface(null, Typeface.BOLD);

        button.setAllCaps(false);

        button.setMinHeight(0);
        button.setMinimumHeight(0);
        button.setMinWidth(0);
        button.setMinimumWidth(0);

        button.setPadding(
                4,
                0,
                4,
                0
        );

        GradientDrawable drawable =
                new GradientDrawable();

        drawable.setColor(backgroundColor);
        drawable.setCornerRadius(8f);

        button.setBackground(drawable);

        button.setElevation(3f);

        return button;
    }

    // ==================================================
    // MEDICINE ITEMS
    // ==================================================

    private void addMedicineItems(
            LinearLayout card,
            Object itemsObject
    ) {

        if (itemsObject == null) {

            card.addView(
                    createTextView(
                            "   • No medicine information",
                            15,
                            DARK_TEXT
                    )
            );

            return;
        }

        if (itemsObject instanceof String) {

            String itemsString =
                    (String) itemsObject;

            try {

                JSONArray items =
                        new JSONArray(itemsString);

                if (items.length() == 0) {

                    card.addView(
                            createTextView(
                                    "   • No medicines",
                                    15,
                                    DARK_TEXT
                            )
                    );

                    return;
                }

                for (int i = 0;
                     i < items.length();
                     i++) {

                    JSONObject item =
                            items.getJSONObject(i);

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

                    card.addView(
                            createTextView(
                                    "   • " +
                                            name +
                                            " × " +
                                            quantity,
                                    15,
                                    DARK_TEXT
                            )
                    );
                }

                return;

            } catch (Exception e) {

                card.addView(
                        createTextView(
                                "   • " + itemsString,
                                15,
                                DARK_TEXT
                        )
                );

                return;
            }
        }

        if (itemsObject instanceof List) {

            List<?> items =
                    (List<?>) itemsObject;

            for (Object object : items) {

                if (object instanceof Map) {

                    Map<?, ?> item =
                            (Map<?, ?>) object;

                    Object nameObject =
                            item.get("name");

                    if (nameObject == null) {
                        nameObject =
                                item.get("medicineName");
                    }

                    Object quantityObject =
                            item.get("quantity");

                    String name =
                            nameObject != null
                                    ? String.valueOf(
                                    nameObject
                            )
                                    : "Medicine";

                    String quantity =
                            quantityObject != null
                                    ? String.valueOf(
                                    quantityObject
                            )
                                    : "1";

                    card.addView(
                            createTextView(
                                    "   • " +
                                            name +
                                            " × " +
                                            quantity,
                                    15,
                                    DARK_TEXT
                            )
                    );
                }
            }
        }
    }

    // ==================================================
    // STATUS ICON
    // ==================================================

    private String getStatusIcon(
            String status
    ) {

        if (status == null) {
            return "🟡";
        }

        switch (status) {

            case "Confirmed":
                return "🟢";

            case "Processing":
                return "🔵";

            case "Parcel Ready":
                return "📦";

            case "Out for Delivery":
                return "🚚";

            case "Delivered":
                return "✅";

            case "Cancelled":
                return "❌";

            default:
                return "🟡";
        }
    }

    // ==================================================
    // STATUS COLOR
    // ==================================================

    private int getStatusColor(
            String status
    ) {

        if (status == null) {
            return BROWN;
        }

        if (status.equalsIgnoreCase("Confirmed")) {
            return GREEN;
        }

        if (status.equalsIgnoreCase("Processing")) {
            return BLUE;
        }

        if (status.equalsIgnoreCase("Parcel Ready")) {
            return ORANGE;
        }

        if (status.equalsIgnoreCase("Out for Delivery")) {
            return BLUE;
        }

        if (status.equalsIgnoreCase("Delivered")) {
            return GREEN;
        }

        if (status.equalsIgnoreCase("Cancelled")) {
            return RED;
        }

        return BROWN;
    }

    // ==================================================
    // TOTAL
    // ==================================================

    private String getTotalText(
            Object totalObject
    ) {

        if (totalObject == null) {
            return "৳0.00";
        }

        if (totalObject instanceof Number) {

            double total =
                    ((Number) totalObject)
                            .doubleValue();

            return "৳" +
                    String.format(
                            Locale.getDefault(),
                            "%.2f",
                            total
                    );
        }

        String total =
                String.valueOf(totalObject);

        if (total.contains("৳")) {
            return total;
        }

        return "৳" + total;
    }

    // ==================================================
    // DATE
    // ==================================================

    private String getOrderDate(
            Object timeObject
    ) {

        if (timeObject instanceof Number) {

            long timestamp =
                    ((Number) timeObject)
                            .longValue();

            SimpleDateFormat formatter =
                    new SimpleDateFormat(
                            "dd MMM yyyy, hh:mm a",
                            Locale.getDefault()
                    );

            return formatter.format(
                    new Date(timestamp)
            );
        }

        return "Date not available";
    }

    // ==================================================
    // TEXT VIEW
    // ==================================================

    private TextView createTextView(
            String text,
            float size,
            int color
    ) {

        TextView textView =
                new TextView(this);

        textView.setText(text);
        textView.setTextSize(size);
        textView.setTextColor(color);

        textView.setPadding(
                0,
                5,
                0,
                5
        );

        return textView;
    }

    // ==================================================
    // MESSAGE
    // ==================================================

    private void showMessage(
            String message
    ) {

        TextView textView =
                new TextView(this);

        textView.setText(message);
        textView.setTextSize(18);
        textView.setTextColor(GRAY);
        textView.setGravity(Gravity.CENTER);

        textView.setPadding(
                20,
                60,
                20,
                60
        );

        orderContainer.addView(textView);
    }
}
