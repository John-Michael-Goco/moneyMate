package com.moneymate.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.moneymate.dashboard.Dashboard;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {

    private static final String URL = "http://192.168.1.6/moneymateBackend/loginUser.php";

    private TextInputLayout emailInput, passwordInput;
    private Button signUpButton, forgotBtn, loginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Handle system insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI elements
        emailInput = findViewById(R.id.loginEmailInput);
        passwordInput = findViewById(R.id.loginPasswordInput);
        signUpButton = findViewById(R.id.signUpBtn);
        forgotBtn = findViewById(R.id.forgotBtn);
        loginBtn = findViewById(R.id.loginBtn);

        // Navigate to Register page
        signUpButton.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, Register.class);
            startActivity(intent);
        });

        // Navigate to Forgot Password page
        forgotBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, ForgotPasswordEmail.class);
            startActivity(intent);
        });

        // Login button listener
        loginBtn.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        // Get input values
        String emailInputVal = emailInput.getEditText().getText().toString().trim();
        String passwordInputVal = passwordInput.getEditText().getText().toString().trim();

        // Validate input fields
        if (emailInputVal.isEmpty() || passwordInputVal.isEmpty()) {
            Toast.makeText(this, "Email and Password are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate email format
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailInputVal).matches()) {
            Toast.makeText(this, "Invalid email address.", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest request = new StringRequest(Request.Method.POST, URL,
                response -> {

                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.optString("status", "error");
                        String message = jsonResponse.optString("message", "Unknown error");

                        if ("success".equals(status)) {
                            Toast.makeText(this, "Logged in successfully!", Toast.LENGTH_SHORT).show();
                            navigateToDashboard();
                        } else {
                            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Log.e("LoginError", "JSON Parsing Error: " + e.getMessage());
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
                params.put("email", emailInputVal);
                params.put("password", passwordInputVal);
                return params;
            }
        };

        // Add the request to the Volley queue
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void navigateToDashboard() {
            Intent intent = new Intent(Login.this, Dashboard.class);

            startActivity(intent);
            finish();
    }
}
