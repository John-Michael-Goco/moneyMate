package com.moneymate.dashboard;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.moneymate.R;
import com.moneymate.auth.Login;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class UserDetails extends AppCompatActivity {

    private static final String URL = "http://192.168.1.6/moneymateBackend/updateUser.php";

    private TextInputEditText emailInput, first_nameInput, last_nameInput, nicknameInput, passwordInput;
    private Button backBtn, updateUserDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_details);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Find Views
        emailInput = findViewById(R.id.emailText);
        first_nameInput = findViewById(R.id.firstNameText);
        last_nameInput = findViewById(R.id.lastNameText);
        nicknameInput = findViewById(R.id.nicknameText);
        passwordInput = findViewById(R.id.passwordText);

        backBtn = findViewById(R.id.backBtn);
        updateUserDetails = findViewById(R.id.updateUserDetails);

        backBtn.setOnClickListener(v -> finish());

        // Retrieve user data
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userID = sharedPreferences.getString("userID", "1");
        String email = sharedPreferences.getString("email", "sample@gmail.com");
        String first_name = sharedPreferences.getString("first_name", "Juan");
        String last_name = sharedPreferences.getString("last_name", "Dela Cruz");
        String nickname = sharedPreferences.getString("nickname", "Tamad");
        String password = sharedPreferences.getString("password", "*********");

        emailInput.setText(email);
        first_nameInput.setText(first_name);
        last_nameInput.setText(last_name);
        nicknameInput.setText(nickname);
        passwordInput.setText(password);

        // Show confirmation dialog before updating
        updateUserDetails.setOnClickListener(v -> showConfirmationDialog(userID));
    }

    private void showConfirmationDialog(String userID) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Update")
                .setMessage("Are you sure you want to update your details?")
                .setPositiveButton("Yes", (dialog, which) -> updateUser(userID))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateUser(String userID) {

        // Get input values
        String firstNameVal = first_nameInput.getText().toString().trim();
        String lastNameVal = last_nameInput.getText().toString().trim();
        String nicknameVal = nicknameInput.getText().toString().trim();
        String passwordVal = passwordInput.getText().toString().trim();

        // Input validation
        if (firstNameVal.isEmpty() || lastNameVal.isEmpty() || nicknameVal.isEmpty() ||
                passwordVal.isEmpty()) {
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create the request to update the student
        StringRequest request = new StringRequest(Request.Method.POST, URL,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");
                        String message = jsonResponse.getString("message");

                        if ("success".equals(status)) {
                            Toast.makeText(this, "User Successfully Updated!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Log.e("UpdateError", "JSON Parsing Error: " + e.getMessage());
                        Toast.makeText(this, "Parsing Error. Try again later.", Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    Log.e("VolleyError", "Request Error: " + error.toString());
                    Toast.makeText(this, "Request Error. Check your connection.", Toast.LENGTH_LONG).show();
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("userID", userID);
                params.put("first_name", firstNameVal);
                params.put("last_name", lastNameVal);
                params.put("nickname", nicknameVal);
                params.put("password", passwordVal);
                return params;
            }
        };

        // Add the request to the Volley queue
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}
