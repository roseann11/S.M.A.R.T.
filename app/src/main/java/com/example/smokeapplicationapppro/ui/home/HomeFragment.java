package com.example.smokeapplicationapppro.ui.home;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.smokeapplicationapppro.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeFragment extends Fragment {

    private TextView tvSafetyStatus, tvSafetyMessage, tvSafetyLevel, tvIs, tvSafetyNumber;
    private ImageView ivHouseGif;
    private ConstraintLayout rootLayout;

    private DatabaseReference sensorRef;
    private GestureDetector gestureDetector;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Make the status bar transparent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            requireActivity().getWindow().setStatusBarColor(Color.TRANSPARENT);
            requireActivity().getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
        }

        // Initialize views
        tvSafetyStatus = view.findViewById(R.id.tv_safety_status);
        tvSafetyMessage = view.findViewById(R.id.tv_message);
        tvSafetyLevel = view.findViewById(R.id.tv_safety_number);
        ivHouseGif = view.findViewById(R.id.iv_house_gif);
        tvIs = view.findViewById(R.id.tv_is);
        tvSafetyNumber = view.findViewById(R.id.tv_safety_number);
        rootLayout = view.findViewById(R.id.root_layout);

        // Set up Firebase listener
        setupFirebaseListener();

        // Initialize GestureDetector
        gestureDetector = new GestureDetector(requireContext(), new GestureDetector.SimpleOnGestureListener() {
            private static final float SWIPE_THRESHOLD = 100;
            private static final float SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                try {
                    float diffX = e1.getX() - e2.getX();
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) { // Left swipe
                            navigateToDashboard();
                            return true;
                        }
                    }
                } catch (Exception e) {
                    Log.e("HomeFragment", "Error processing gesture", e);
                }
                return false;
            }
        });

        // Set touch listener for swipe gestures
        rootLayout.setOnTouchListener((v, event) -> {
            v.performClick();
            return gestureDetector.onTouchEvent(event);
        });

        return view;
    }

    private void setupFirebaseListener() {
        // Initialize Firebase reference
        sensorRef = FirebaseDatabase.getInstance()
                .getReferenceFromUrl("https://smokedetectiondatabase-default-rtdb.firebaseio.com/sensor");

        sensorRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Extract data from Firebase
                    String buzzerStatus = snapshot.child("buzzerStatus").getValue(String.class);
                    String ledStatus = snapshot.child("ledStatus").getValue(String.class);
                    Integer mq2Value = snapshot.child("mq2Value").getValue(Integer.class);

                    // Update the UI
                    updateUI(mq2Value, buzzerStatus, ledStatus);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvSafetyMessage.setText("Error fetching data from database.");
                tvSafetyStatus.setTextColor(Color.RED);
            }
        });
    }

    private void updateUI(Integer mq2Value, String buzzerStatus, String ledStatus) {
        // Update safety level
        tvSafetyLevel.setText(String.valueOf(mq2Value));

        // Determine safety status and update UI based on ledStatus
        if ("green".equals(ledStatus)) {
            tvIs.setText("is");
            tvSafetyStatus.setText("Safe");
            tvSafetyStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.green));
            Glide.with(this)
                    .asGif()
                    .load(R.drawable.safe_house) // Replace with your actual GIF resource
                    .into(ivHouseGif);
            tvSafetyMessage.setText("Everything is okay. No smoke detected.");
            tvSafetyNumber.setTextColor(ContextCompat.getColor(requireContext(), R.color.green));
        } else if ("yellow".equals(ledStatus)) {
            tvIs.setText("is in");
            tvSafetyStatus.setText("Warning");
            tvSafetyStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.yellow));
            Glide.with(this)
                    .asGif()
                    .load(R.drawable.smoke_house) // Replace with your actual GIF resource
                    .into(ivHouseGif); // Replace with your warning gif
            tvSafetyMessage.setText("Smoke detected! Stay alert.");
            tvSafetyNumber.setTextColor(ContextCompat.getColor(requireContext(), R.color.yellow));
        } else if ("red".equals(ledStatus)) {
            tvIs.setText("is in");
            tvSafetyStatus.setText("Danger");
            tvSafetyStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.red));
            Glide.with(this)
                    .asGif()
                    .load(R.drawable.fire_house) // Replace with your actual GIF resource
                    .into(ivHouseGif); // Replace with your danger gif
            tvSafetyMessage.setText("Danger! Immediate action required!");
            tvSafetyNumber.setTextColor(ContextCompat.getColor(requireContext(), R.color.red));
        }

        // Optionally log or display buzzer status
        Log.d("HomeFragment", "Buzzer Status: " + buzzerStatus);
    }

    private void navigateToDashboard() {
        try {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
            navController.navigate(R.id.action_navigation_home_to_navigation_dashboard);
        } catch (Exception e) {
            Log.e("HomeFragment", "Navigation error", e);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        sensorRef = null; // Clean up Firebase listener reference
    }
}