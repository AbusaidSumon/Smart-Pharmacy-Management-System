package com.example.smartpharmacy;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationActivity extends AppCompatActivity {

    private LinearLayout notificationContainer;

    private Button btnRefresh;
    private Button btnBack;
    private Button btnMarkAllRead;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private static final int DARK_GREEN =
            Color.rgb(23, 74, 69);

    private static final int GREEN =
            Color.rgb(40, 124, 112);

    private static final int DARK_TEXT =
            Color.rgb(65, 65, 65);

    private static final int GRAY =
            Color.rgb(120, 120, 120);

    private static final int RED =
            Color.rgb(190, 60, 60);

    private static final int LIGHT_GREEN =
            Color.rgb(225, 239, 236);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_notification
        );

        auth =
                FirebaseAuth.getInstance();

        db =
                FirebaseFirestore.getInstance();

        notificationContainer =
                findViewById(
                        R.id.notificationContainer
                );

        btnRefresh =
                findViewById(
                        R.id.btnRefresh
                );

        btnMarkAllRead =
                findViewById(
                        R.id.btnMarkAllRead
                );

        btnBack =
                findViewById(
                        R.id.btnBack
                );

        loadNotifications();

        btnRefresh.setOnClickListener(
                v -> loadNotifications()
        );

        btnMarkAllRead.setOnClickListener(
                v -> markAllAsRead()
        );

        btnBack.setOnClickListener(
                v -> finish()
        );
    }

    // ==================================================
    // LOAD NOTIFICATIONS
    // ==================================================

    private void loadNotifications() {

        notificationContainer.removeAllViews();

        FirebaseUser currentUser =
                auth.getCurrentUser();

        if (currentUser == null) {

            showMessage(
                    "Please login first."
            );

            return;
        }

        showMessage(
                "Loading notifications..."
        );

        String userId =
                currentUser.getUid();

        db.collection("notifications")
                .whereEqualTo(
                        "userId",
                        userId
                )
                .get()
                .addOnSuccessListener(
                        queryDocumentSnapshots -> {

                            notificationContainer
                                    .removeAllViews();

                            List<DocumentSnapshot>
                                    notifications =
                                    new ArrayList<>(
                                            queryDocumentSnapshots
                                                    .getDocuments()
                                    );

                            notifications.sort(
                                    Comparator.comparingLong(
                                            this::getTimestamp
                                    ).reversed()
                            );

                            if (notifications.isEmpty()) {

                                showMessage(
                                        "🔔 No notifications yet."
                                );

                                return;
                            }

                            for (
                                    DocumentSnapshot document :
                                    notifications
                            ) {

                                addNotificationCard(
                                        document
                                );
                            }
                        }
                )
                .addOnFailureListener(
                        e -> {

                            notificationContainer
                                    .removeAllViews();

                            showMessage(
                                    "Unable to load notifications."
                            );

                            Toast.makeText(
                                    this,
                                    "Error: " +
                                            e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    // ==================================================
    // ADD NOTIFICATION CARD
    // ==================================================

    private void addNotificationCard(
            DocumentSnapshot document
    ) {

        String title =
                document.getString("title");

        String message =
                document.getString("message");

        String orderId =
                document.getString("orderId");

        String status =
                document.getString("status");

        Boolean read =
                document.getBoolean("read");

        if (title == null) {
            title = "Notification";
        }

        if (message == null) {
            message = "";
        }

        if (orderId == null) {
            orderId = "";
        }

        if (status == null) {
            status = "";
        }

        boolean isRead =
                read != null && read;

        // ==================================================
        // CARD
        // ==================================================

        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.VERTICAL
        );

        card.setPadding(
                18,
                18,
                18,
                18
        );

        GradientDrawable background =
                new GradientDrawable();

        if (isRead) {
            background.setColor(
                    Color.WHITE
            );
        } else {
            background.setColor(
                    LIGHT_GREEN
            );
        }

        background.setCornerRadius(14);

        card.setBackground(background);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(
                0,
                0,
                0,
                15
        );

        card.setLayoutParams(params);

        // ==================================================
        // TITLE
        // ==================================================

        TextView txtTitle =
                createText(
                        title,
                        18,
                        getNotificationColor(status)
                );

        txtTitle.setTypeface(
                null,
                Typeface.BOLD
        );

        card.addView(txtTitle);

        // ==================================================
        // MESSAGE
        // ==================================================

        card.addView(
                createText(
                        message,
                        15,
                        DARK_TEXT
                )
        );

        // ==================================================
        // ORDER ID
        // ==================================================

        if (!orderId.isEmpty()) {

            card.addView(
                    createText(
                            "📦 Order ID: " +
                                    orderId,
                            14,
                            DARK_GREEN
                    )
            );
        }

        // ==================================================
        // STATUS
        // ==================================================

        if (!status.isEmpty()) {

            card.addView(
                    createText(
                            "Status: " +
                                    getStatusIcon(status) +
                                    " " +
                                    status,
                            14,
                            getNotificationColor(
                                    status
                            )
                    )
            );
        }

        // ==================================================
        // DATE
        // ==================================================

        card.addView(
                createText(
                        "🕒 " +
                                getDate(
                                        document.get(
                                                "timestamp"
                                        )
                                ),
                        13,
                        GRAY
                )
        );

        // ==================================================
        // READ STATUS
        // ==================================================

        TextView readText =
                createText(
                        isRead
                                ? "✓ Read"
                                : "● Unread",
                        13,
                        isRead
                                ? GRAY
                                : RED
                );

        readText.setTypeface(
                null,
                Typeface.BOLD
        );

        card.addView(readText);

        // ==================================================
        // CLICK
        // ==================================================

        card.setOnClickListener(
                v -> {

                    if (!isRead) {

                        db.collection(
                                        "notifications"
                                )
                                .document(
                                        document.getId()
                                )
                                .update(
                                        "read",
                                        true
                                )
                                .addOnSuccessListener(
                                        unused ->
                                                loadNotifications()
                                );
                    }
                }
        );

        notificationContainer.addView(
                card
        );
    }

    // ==================================================
    // MARK ALL READ
    // ==================================================

    private void markAllAsRead() {

        FirebaseUser currentUser =
                auth.getCurrentUser();

        if (currentUser == null) {
            return;
        }

        String userId =
                currentUser.getUid();

        db.collection("notifications")
                .whereEqualTo(
                        "userId",
                        userId
                )
                .get()
                .addOnSuccessListener(
                        queryDocumentSnapshots -> {

                            int count = 0;

                            for (
                                    DocumentSnapshot document :
                                    queryDocumentSnapshots
                                            .getDocuments()
                            ) {

                                Boolean read =
                                        document.getBoolean(
                                                "read"
                                        );

                                if (read == null || !read) {

                                    count++;

                                    db.collection(
                                                    "notifications"
                                            )
                                            .document(
                                                    document.getId()
                                            )
                                            .update(
                                                    "read",
                                                    true
                                            );
                                }
                            }

                            Toast.makeText(
                                    this,
                                    count > 0
                                            ? "All notifications marked as read."
                                            : "No unread notifications.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            loadNotifications();
                        }
                );
    }

    // ==================================================
    // TIMESTAMP
    // ==================================================

    private long getTimestamp(
            DocumentSnapshot document
    ) {

        Object value =
                document.get("timestamp");

        if (value instanceof Number) {

            return ((Number) value)
                    .longValue();
        }

        return 0;
    }

    // ==================================================
    // DATE
    // ==================================================

    private String getDate(
            Object value
    ) {

        if (value instanceof Number) {

            long timestamp =
                    ((Number) value)
                            .longValue();

            SimpleDateFormat format =
                    new SimpleDateFormat(
                            "dd MMM yyyy, hh:mm a",
                            Locale.getDefault()
                    );

            return format.format(
                    new Date(timestamp)
            );
        }

        return "Date not available";
    }

    // ==================================================
    // COLOR
    // ==================================================

    private int getNotificationColor(
            String status
    ) {

        if (status == null) {
            return DARK_GREEN;
        }

        switch (status) {

            case "Confirmed":
                return GREEN;

            case "Processing":
                return Color.rgb(
                        52,
                        101,
                        164
                );

            case "Parcel Ready":
                return Color.rgb(
                        225,
                        132,
                        45
                );

            case "Out for Delivery":
                return Color.rgb(
                        52,
                        101,
                        164
                );

            case "Delivered":
                return GREEN;

            case "Cancelled":
                return RED;

            default:
                return DARK_GREEN;
        }
    }

    // ==================================================
    // ICON
    // ==================================================

    private String getStatusIcon(
            String status
    ) {

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
    // TEXT
    // ==================================================

    private TextView createText(
            String text,
            float size,
            int color
    ) {

        TextView view =
                new TextView(this);

        view.setText(text);
        view.setTextSize(size);
        view.setTextColor(color);

        view.setPadding(
                0,
                4,
                0,
                4
        );

        return view;
    }

    // ==================================================
    // MESSAGE
    // ==================================================

    private void showMessage(
            String message
    ) {

        TextView view =
                createText(
                        message,
                        18,
                        GRAY
                );

        view.setGravity(
                Gravity.CENTER
        );

        view.setPadding(
                20,
                60,
                20,
                60
        );

        notificationContainer.addView(
                view
        );
    }
}