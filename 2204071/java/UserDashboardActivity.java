package com.example.smartpharmacy;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
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

public class UserDashboardActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    private FirebaseFirestore db;

    private TextView txtNotificationBadge;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_user_dashboard
        );


        // ==========================
        // FIREBASE AUTH
        // ==========================

        auth =
                FirebaseAuth.getInstance();


        // ==========================
        // FIRESTORE
        // ==========================

        db =
                FirebaseFirestore.getInstance();


        // ==========================
        // FIND VIEWS
        // ==========================

        LinearLayout cardMedicine =
                findViewById(
                        R.id.cardMedicine
                );

        LinearLayout cardOrder =
                findViewById(
                        R.id.cardOrder
                );

        LinearLayout cardCart =
                findViewById(
                        R.id.cardCart
                );

        LinearLayout cardProfile =
                findViewById(
                        R.id.cardProfile
                );

        LinearLayout cardNotification =
                findViewById(
                        R.id.cardNotification
                );

        Button btnLogout =
                findViewById(
                        R.id.btnLogout
                );

        txtNotificationBadge =
                findViewById(
                        R.id.txtNotificationBadge
                );


        // ==========================
        // MAKE BADGE CIRCLE
        // ==========================

        makeBadgeCircle();


        // ==========================
        // CHECK UNREAD NOTIFICATIONS
        // ==========================

        checkUnreadNotifications();


        // ==========================
        // MEDICINE
        // ==========================

        cardMedicine.setOnClickListener(
                v -> {

                    Intent intent =
                            new Intent(
                                    UserDashboardActivity.this,
                                    MedicineActivity.class
                            );

                    startActivity(intent);
                }
        );


        // ==========================
        // MY ORDERS
        // ==========================

        cardOrder.setOnClickListener(
                v -> {

                    Intent intent =
                            new Intent(
                                    UserDashboardActivity.this,
                                    MyOrdersActivity.class
                            );

                    startActivity(intent);
                }
        );


        // ==========================
        // CART
        // ==========================

        cardCart.setOnClickListener(
                v -> {

                    Intent intent =
                            new Intent(
                                    UserDashboardActivity.this,
                                    CartActivity.class
                            );

                    startActivity(intent);
                }
        );


        // ==========================
        // PROFILE
        // ==========================

        cardProfile.setOnClickListener(
                v -> {

                    Intent intent =
                            new Intent(
                                    UserDashboardActivity.this,
                                    ProfileActivity.class
                            );

                    startActivity(intent);
                }
        );


        // ==========================
        // NOTIFICATIONS
        // ==========================

        cardNotification.setOnClickListener(
                v -> {

                    Intent intent =
                            new Intent(
                                    UserDashboardActivity.this,
                                    NotificationActivity.class
                            );

                    startActivity(intent);
                }
        );


        // ==========================
        // LOGOUT
        // ==========================

        btnLogout.setOnClickListener(
                v -> {

                    auth.signOut();

                    Toast.makeText(
                            UserDashboardActivity.this,
                            "Logged out successfully",
                            Toast.LENGTH_SHORT
                    ).show();


                    Intent intent =
                            new Intent(
                                    UserDashboardActivity.this,
                                    MainActivity.class
                            );


                    intent.setFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK |
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                    );


                    startActivity(intent);

                    finish();
                }
        );
    }


    // =========================================================
    // MAKE BADGE CIRCLE
    // =========================================================

    private void makeBadgeCircle() {

        if (txtNotificationBadge == null) {
            return;
        }


        GradientDrawable badgeBackground =
                new GradientDrawable();

        badgeBackground.setShape(
                GradientDrawable.OVAL
        );

        badgeBackground.setColor(
                Color.rgb(211, 47, 47)
        );

        badgeBackground.setStroke(
                2,
                Color.WHITE
        );


        txtNotificationBadge.setBackground(
                badgeBackground
        );
    }


    // =========================================================
    // CHECK UNREAD NOTIFICATIONS
    // =========================================================

    private void checkUnreadNotifications() {

        FirebaseUser currentUser =
                auth.getCurrentUser();


        // ==========================
        // NO USER
        // ==========================

        if (currentUser == null) {

            hideNotificationBadge();

            return;
        }


        String currentUserId =
                currentUser.getUid();


        // =====================================================
        // GET USER NOTIFICATIONS
        // =====================================================

        db.collection("notifications")

                .whereEqualTo(
                        "userId",
                        currentUserId
                )

                .get()

                .addOnSuccessListener(
                        queryDocumentSnapshots -> {

                            int unreadCount = 0;


                            // =================================================
                            // CHECK EVERY NOTIFICATION
                            // =================================================

                            for (
                                    DocumentSnapshot document :
                                    queryDocumentSnapshots.getDocuments()
                            ) {

                                Object readObject =
                                        document.get("read");


                                boolean isRead = false;


                                // ==============================
                                // FIRESTORE BOOLEAN
                                // ==============================

                                if (
                                        readObject instanceof Boolean
                                ) {

                                    isRead =
                                            (Boolean) readObject;
                                }


                                // ==============================
                                // STRING VALUE
                                // ==============================

                                else if (
                                        readObject instanceof String
                                ) {

                                    isRead =
                                            Boolean.parseBoolean(
                                                    (String) readObject
                                            );
                                }


                                // ==============================
                                // UNREAD
                                // ==============================

                                if (!isRead) {

                                    unreadCount++;
                                }
                            }


                            // =================================================
                            // SHOW BADGE
                            // =================================================

                            if (unreadCount > 0) {

                                showNotificationBadge(
                                        unreadCount
                                );

                            } else {

                                hideNotificationBadge();
                            }
                        }
                )

                .addOnFailureListener(
                        e -> {

                            hideNotificationBadge();
                        }
                );
    }


    // =========================================================
    // SHOW BADGE
    // =========================================================

    private void showNotificationBadge(
            int unreadCount
    ) {

        if (txtNotificationBadge == null) {
            return;
        }


        if (unreadCount > 99) {

            txtNotificationBadge.setText(
                    "99+"
            );

        } else {

            txtNotificationBadge.setText(
                    String.valueOf(
                            unreadCount
                    )
            );
        }


        txtNotificationBadge.setVisibility(
                View.VISIBLE
        );
    }


    // =========================================================
    // HIDE BADGE
    // =========================================================

    private void hideNotificationBadge() {

        if (txtNotificationBadge == null) {
            return;
        }


        txtNotificationBadge.setVisibility(
                View.GONE
        );
    }


    // =========================================================
    // CHECK AGAIN WHEN RETURNING
    // =========================================================

    @Override
    protected void onResume() {

        super.onResume();


        if (
                auth != null
                        &&
                        txtNotificationBadge != null
        ) {

            checkUnreadNotifications();
        }
    }
}