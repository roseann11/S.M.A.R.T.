package com.example.smokeapplicationapppro.ui.home;




import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.example.smokeapplicationapppro.R;
import com.example.smokeapplicationapppro.databinding.FragmentDashboardBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.telephony.SmsManager;
import android.widget.Switch;


import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;


public class DashboardFragment extends Fragment {
    private FragmentDashboardBinding binding;
    private GestureDetector gestureDetector;
    private TextView tvSafetyStatus, tvSafetyMessage, tvSafetyLevel, tvIs;
    private ImageView ivHouseGif;
    private ConstraintLayout rootLayout;
    private DatabaseReference sensorReference;
    private TextView lastUpdateTextView;
    private DatabaseReference contactsReference;
    private Switch sosSwitch;
    private CardView cardMq2, cardBuzzer, cardLed;
    private TextView mq2ValueText, buzzerStatusText, ledStatusText;
    private static final int SMS_PERMISSION_REQUEST_CODE = 123;
    private List<EmergencyContact> emergencyContacts = new ArrayList<>();




    private static class EmergencyContact {
        String name;
        String number;




        EmergencyContact(String name, String number) {
            this.name = name;
            this.number = number;
        }
    }




    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        // Initialize Firebase references
        sensorReference = FirebaseDatabase.getInstance().getReference("sensor");


        // Get the current user from Firebase Authentication
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid(); // Dynamically fetch the logged-in user's ID
            contactsReference = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(userId) // Use the dynamically fetched userId
                    .child("emergency_contacts");
        } else {
            Log.e("DashboardFragment", "User not logged in");
            showToast("User not logged in");
            return root; // Exit early or handle the error accordingly
        }


        // Initialize views and setup listeners
        initializeViews(root);
        setupGestureDetector();
        setupFirebaseListener();
        setupSosSwitch();
        loadEmergencyContacts();


        return root;
    }






    private void initializeViews(View root) {
        // Initialize card views
        cardMq2 = root.findViewById(R.id.card_mq2);
        cardBuzzer = root.findViewById(R.id.card_buzzer);
        cardLed = root.findViewById(R.id.card_led);




        // Initialize text views inside cards
        mq2ValueText = ((TextView) ((LinearLayout) cardMq2.getChildAt(0)).getChildAt(1));
        buzzerStatusText = ((TextView) ((LinearLayout) cardBuzzer.getChildAt(0)).getChildAt(1));
        ledStatusText = ((TextView) ((LinearLayout) cardLed.getChildAt(0)).getChildAt(1));




        lastUpdateTextView = binding.textLastUpdate;
        sosSwitch = root.findViewById(R.id.sosSwitchId);




    }






    private void setupFirebaseListener() {
        sensorReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Integer mq2Value = dataSnapshot.child("mq2Value").getValue(Integer.class);
                    String buzzerStatus = dataSnapshot.child("buzzerStatus").getValue(String.class);
                    String ledStatus = dataSnapshot.child("ledStatus").getValue(String.class);




                    updateMq2Value(mq2Value);
                    updateBuzzerStatus(buzzerStatus);
                    updateLedStatus(ledStatus);




                    // Get the current date and time in Month DD, YYYY format
                    SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
                    String currentDateAndTime = sdf.format(new Date());




                    // Set the formatted date to the Last Update text view
                    TextView lastUpdateTextView = getView().findViewById(R.id.text_last_update);
                    lastUpdateTextView.setText(currentDateAndTime);
                }
            }




            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("DashboardFragment", "Firebase Database error: " + error.getMessage());
                showToast("Failed to update sensor data");
            }
        });
    }




    private void updateMq2Value(Integer value) {
        if (value != null) {
            mq2ValueText.setText(String.valueOf(value));
        }
    }




    private void updateBuzzerStatus(String status) {
        if (status != null) {
            if ("on".equalsIgnoreCase(status)) {
                cardBuzzer.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.green));
                buzzerStatusText.setText("ONLINE");
            } else {
                cardBuzzer.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.grey));
                buzzerStatusText.setText("OFFLINE");
            }
        }
    }




    private void updateLedStatus(String status) {
        if (status != null) {
            switch (status.toLowerCase()) {
                case "green":
                    cardLed.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.green));
                    ledStatusText.setText("SAFE");
                    break;
                case "yellow":
                    cardLed.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.yellow));
                    ledStatusText.setText("WARNING");
                    if (sosSwitch.isChecked()) {
                        sendSosMessages();
                    }
                    break;
                case "red":
                    cardLed.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.red));
                    ledStatusText.setText("DANGER");
                    if (sosSwitch.isChecked()) {
                        sendSosMessages();
                    }
                    break;
            }
        }
    }








    private void setupGestureDetector() {
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
                        if (diffX < 0) {
                            navigateToHome();
                            return true;
                        }
                    }
                } catch (Exception e) {
                    Log.e("DashboardFragment", "Error processing gesture", e);
                }
                return false;
            }
        });




        binding.getRoot().setOnTouchListener((v, event) -> {
            v.performClick();
            return gestureDetector.onTouchEvent(event);
        });
    }




    private void setupSosSwitch() {
        // Load the saved state from SharedPreferences
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("SmokeAppPrefs", Context.MODE_PRIVATE);
        boolean isSosOn = sharedPreferences.getBoolean("sos_switch_state", false);
        sosSwitch.setChecked(isSosOn);


        // Set up the listener to save the state when it changes
        sosSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (checkSmsPermission()) {
                    sendSosMessages();
                    sosSwitch.setChecked(true);
                } else {
                    requestSmsPermission();
                    sosSwitch.setChecked(false);
                }
            }


            // Save the new state in SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("sos_switch_state", isChecked);
            editor.apply();
        });
    }




    private void loadEmergencyContacts() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();  // Get the user ID


            // Reference to the user's emergency contacts in Firebase
            DatabaseReference contactsReference = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(userId)
                    .child("emergency_contacts");


            // Add a listener to retrieve contacts
            contactsReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    emergencyContacts.clear(); // Clear the current list of contacts


                    // Check if contacts are available
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot contactSnapshot : dataSnapshot.getChildren()) {
                            String name = contactSnapshot.child("name").getValue(String.class);
                            String number = contactSnapshot.child("number").getValue(String.class);


                            if (name != null && number != null) {
                                emergencyContacts.add(new EmergencyContact(name, number));
                            }
                        }
                    }


                    // Check if no contacts were loaded
                    if (emergencyContacts.isEmpty()) {
                        showToast("No emergency contacts found");
                    } else {
                        showToast("Emergency contacts loaded");
                    }
                }


                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("DashboardFragment", "Failed to load emergency contacts: " + error.getMessage());
                    showToast("Failed to load emergency contacts");
                }
            });
        }
    }
    private void saveEmergencyContactsToSharedPreferences() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("SmokeAppPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();


        Gson gson = new Gson();
        String emergencyContactsJson = gson.toJson(emergencyContacts);  // Convert the list to JSON
        editor.putString("emergency_contacts", emergencyContactsJson);
        editor.apply();
    }


    private void loadEmergencyContactsFromSharedPreferences() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("SmokeAppPrefs", Context.MODE_PRIVATE);
        String emergencyContactsJson = sharedPreferences.getString("emergency_contacts", null);


        if (emergencyContactsJson != null) {
            // Deserialize the contacts from JSON string
            Gson gson = new Gson();
            Type type = new TypeToken<List<EmergencyContact>>(){}.getType();
            emergencyContacts = gson.fromJson(emergencyContactsJson, type);
        }


        if (emergencyContacts.isEmpty()) {
            showToast("No emergency contacts found in preferences");
        } else {
            showToast("Emergency contacts loaded from preferences");
        }
    }








    private void sendSosMessages() {
        sensorReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Integer mq2Value = dataSnapshot.child("mq2Value").getValue(Integer.class);
                    String ledStatus = dataSnapshot.child("ledStatus").getValue(String.class);




                    if ("yellow".equalsIgnoreCase(ledStatus) || "red".equalsIgnoreCase(ledStatus)) {
                        String message = createEmergencyMessage(mq2Value, ledStatus);
                        sendMessagesToContacts(message);
                    } else {
                        Log.d("DashboardFragment", "LED status is green or invalid; no SOS message sent.");
                        showToast("No emergency detected; SOS message not sent.");
                    }
                }
            }




            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("DashboardFragment", "Failed to read sensor data: " + error.getMessage());
                showToast("Failed to send SOS messages");
            }
        });
    }








    private String createEmergencyMessage(Integer mq2Value, String ledStatus) {
        StringBuilder message = new StringBuilder();
        message.append("🚨 EMERGENCY ALERT! 🚨\n\n");
        message.append("Smoke Detection System Alert:\n");
        message.append("Current MQ2 Sensor Reading: ").append(mq2Value != null ? mq2Value : "N/A").append("\n");
        message.append("Status: ").append(ledStatus != null ? ledStatus.toUpperCase() : "UNKNOWN").append("\n\n");




        // Adjust the message based on the status
        if ("red".equalsIgnoreCase(ledStatus)) {
            message.append("Immediate assistance may be required!");
        } else if ("yellow".equalsIgnoreCase(ledStatus)) {
            message.append("Please remain cautious and monitor the situation.");
        } else if ("green".equalsIgnoreCase(ledStatus)) {
            message.append("All systems are functioning normally. Stay safe!");
        } else {
            message.append("Unknown status. Please check the system.");
        }




        return message.toString();
    }








    private void sendMessagesToContacts(String message) {
        if (emergencyContacts.isEmpty()) {
            showToast("No emergency contacts found");
            return;
        }




        SmsManager smsManager = SmsManager.getDefault();
        int successCount = 0;




        for (EmergencyContact contact : emergencyContacts) {
            try {
                ArrayList<String> parts = smsManager.divideMessage(message);
                smsManager.sendMultipartTextMessage(
                        contact.number,
                        null,
                        parts,
                        null,
                        null
                );
                successCount++;
                Log.d("DashboardFragment", "SOS message sent to " + contact.name);
            } catch (Exception e) {
                Log.e("DashboardFragment", "Failed to send SOS to " + contact.name + ": " + e.getMessage());
            }
        }




        // Adjust the message based on the number of contacts
        String resultMessage = successCount > 0
                ? "SOS message sent to " + successCount + (successCount == 1 ? " contact" : " contacts")
                : "Failed to send SOS messages";
        showToast(resultMessage);
    }








    private boolean checkSmsPermission() {
        return ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }




    private void requestSmsPermission() {
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{Manifest.permission.SEND_SMS},
                SMS_PERMISSION_REQUEST_CODE);
    }




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendSosMessages();
            } else {
                showToast("SMS permission denied. Cannot send SOS messages.");
                sosSwitch.setChecked(false);
            }
        }
    }




    private void navigateToHome() {
        try {
            NavController navController = Navigation.findNavController(requireActivity(),
                    R.id.nav_host_fragment_activity_main);
            navController.navigate(R.id.action_navigation_dashboard_to_navigation_home);
        } catch (Exception e) {
            Log.e("DashboardFragment", "Navigation error", e);
        }
    }




    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }




    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}



