package com.moneymate.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
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
import com.google.android.material.textfield.TextInputLayout;
import com.moneymate.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {
    private static final String URL = "http://10.0.2.2/moneymateBackend/insertUser.php";

    // Input fields and buttons
    private TextInputLayout firstName, lastName, nickname, email, password;
    private Button signUpBtn, signInBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        // Initialize UI elements
        signInBtn = findViewById(R.id.signInBtn);
        signUpBtn = findViewById(R.id.signUpBtn);

        firstName = findViewById(R.id.firstName);
        lastName = findViewById(R.id.lastName);
        nickname = findViewById(R.id.nickname);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);

        // Sign Up button listener
        signUpBtn.setOnClickListener(v -> registerUser());

        // Navigate to the Login page
        signInBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Register.this, Login.class);
            startActivity(intent);
            finish();
        });

        // Handle window insets for proper layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void registerUser() {
        // Get input values
        String firstNameVal = firstName.getEditText().getText().toString().trim();
        String lastNameVal = lastName.getEditText().getText().toString().trim();
        String nicknameVal = nickname.getEditText().getText().toString().trim();
        String emailVal = email.getEditText().getText().toString().trim();
        String passwordVal = password.getEditText().getText().toString().trim();

        // Validate input fields
        if (firstNameVal.isEmpty() || lastNameVal.isEmpty() || nicknameVal.isEmpty() ||
                emailVal.isEmpty() || passwordVal.isEmpty()) {
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate email format
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailVal).matches()) {
            Toast.makeText(this, "Invalid email address.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create the request to register the user
        StringRequest request = new StringRequest(Request.Method.POST, URL,
                response -> {
                    try {

                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");
                        String message = jsonResponse.getString("message");

                        if ("exists".equals(status)) {
                            Toast.makeText(this, "User already registered.", Toast.LENGTH_SHORT).show();
                        } else if ("success".equals(status)) {
                            Toast.makeText(this, "Registered successfully!", Toast.LENGTH_SHORT).show();
                            navigateToLogin();
                        } else {
                            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Log.e("RegisterError", "JSON Parsing Error: " + e.getMessage());
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
                params.put("first_name", firstNameVal);
                params.put("last_name", lastNameVal);
                params.put("nickname", nicknameVal);
                params.put("email", emailVal);
                params.put("password", passwordVal);
                return params;
            }
        };

        // Add the request to the Volley queue
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(Register.this, Login.class);
        startActivity(intent);
        finish();
    }
}
