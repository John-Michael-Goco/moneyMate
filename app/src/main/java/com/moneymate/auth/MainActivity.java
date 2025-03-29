package com.moneymate.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.moneymate.R;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private static final int SPLASH_SCREEN = 3000; // Splash duration

    // Variables
    private Animation logoAnim;
    private ImageView logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Load animation from XML
        logoAnim = AnimationUtils.loadAnimation(this, R.anim.logo_anim);

        // Find the ImageView in the layout
        logo = findViewById(R.id.logo);

        // Apply the animation to the ImageView
        logo.setAnimation(logoAnim);

        // Delay for splash screen, then move to Login activity
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this, Login.class);
                startActivity(intent);
                finish();
            }
        }, SPLASH_SCREEN);

        // Handle window insets to adjust padding for system bars (status bar, navigation bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Check database connection
        checkDatabaseConnection();
    }

    // Database connection check
    private void checkDatabaseConnection() {
        String url = "http://192.168.1.6/moneymateBackend/checkdbconnection.php";

        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String status = jsonObject.getString("status");

                            if (status.equals("success")) {
                                Toast.makeText(MainActivity.this, "Connected to Database", Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(MainActivity.this, "Database Connection Failed!: ", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "Connection Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        error.printStackTrace();
                    }
                });
        queue.add(request);
    }
}