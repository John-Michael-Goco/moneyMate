package com.moneymate.dashboard;

import static android.view.View.INVISIBLE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.moneymate.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ViewGoal extends AppCompatActivity {
    private static final String fetchGoalDetailsURL = "http://10.0.2.2/moneymateBackend/fetchGoalDetails.php";
    private static final String deleteGoalURL = "http://10.0.2.2/moneymateBackend/deleteGoal.php";
    private static final String completeGoalURL = "http://10.0.2.2/moneymateBackend/completeGoal.php";
    private ImageView backBtn, editBtn, deleteBtn, markCompleteBtn;
    private TextView goalNameText, accountNameText, goalStatusText, progressText, createdAtText, goalDateText;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_goal);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get necessary IDs
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userID = sharedPreferences.getString("userID", "1");
        String goalID = getIntent().getStringExtra("goalID");

        // UI references
        backBtn = findViewById(R.id.backBtn);
        markCompleteBtn = findViewById(R.id.markCompleteBtn);
        editBtn = findViewById(R.id.editBtn);
        deleteBtn = findViewById(R.id.deleteBtn);
        goalNameText = findViewById(R.id.goalNameText);
        accountNameText = findViewById(R.id.accountNameText);
        goalStatusText = findViewById(R.id.goalStatusText);
        progressText = findViewById(R.id.progressText);
        progressBar = findViewById(R.id.progressBar);
        createdAtText = findViewById(R.id.createdAtText);
        goalDateText = findViewById(R.id.goalDateText);

        // Handle Back Button
        backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ViewGoal.this, Dashboard.class);
            intent.putExtra("fragmentToOpen", "Goals");
            startActivity(intent);
            finish();
        });
        // Handle Delete Button
        deleteBtn.setOnClickListener(v -> showDeleteConfirmationDialog(goalID));
        // Handle Mark Complete Button
        markCompleteBtn.setOnClickListener(v -> showCompleteConfirmationDialog(goalID));
        // Handle Edit Button
        editBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ViewGoal.this, EditGoals.class);
            intent.putExtra("goalID", goalID);
            startActivity(intent);
            finish();
        });
        // Fetch data
        fetchGoalDetails(goalID, userID);
    }
    private void fetchGoalDetails(String goal_id, String user_id) {
        RequestQueue requestQueue = Volley.newRequestQueue(ViewGoal.this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, fetchGoalDetailsURL,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);

                        if (jsonResponse.getString("status").equals("success")) {
                            JSONObject goalObject = jsonResponse.getJSONObject("goalDetails");
                            String goalTitle = goalObject.getString("goal_title");
                            String accountName = goalObject.getString("account_name");
                            double amount = Double.parseDouble(goalObject.getString("amount"));
                            String goalCompletion = goalObject.getString("goal_completion");
                            String startDate = goalObject.getString("start_date");
                            String targetDate = goalObject.getString("target_date");
                            String completedAt = goalObject.optString("completed_at", null);
                            double accountBalance = Double.parseDouble(goalObject.optString("account_balance", "0"));

                            // Format Created at Like MM/YYYY
                            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM dd yyyy", Locale.getDefault());

                            Date start_date = inputFormat.parse(startDate);
                            Date target_date = inputFormat.parse(targetDate);
                            String formattedStartDate = outputFormat.format(start_date);
                            String formattedTargetDate = outputFormat.format(target_date);
                            String formattedCompletedAtDate = "";

                            if (completedAt != null && !completedAt.equals("null") && !completedAt.isEmpty()) {
                                Date completed_at = inputFormat.parse(completedAt);
                                formattedCompletedAtDate = outputFormat.format(completed_at);
                            }

                            goalNameText.setText(goalTitle);
                            accountNameText.setText("Associated with: " + accountName);
                            goalStatusText.setText(goalCompletion);
                            createdAtText.setText("Started on: " + formattedStartDate);

                            if ("Complete".equals(goalCompletion) && !formattedCompletedAtDate.isEmpty()) {
                                goalDateText.setText("Completed on: " + formattedCompletedAtDate);
                                goalStatusText.setTextColor(this.getColor(R.color.green));
                                accountBalance = amount;
                                editBtn.setVisibility(INVISIBLE);
                                markCompleteBtn.setVisibility(INVISIBLE);
                            } else {
                                goalDateText.setText("Target date: " + formattedTargetDate);
                                goalStatusText.setTextColor(this.getColor(R.color.cerulean));
                            }

                            // Compare with today
                            if ("Ongoing".equals(goalCompletion)) {
                                Date today = new Date();
                                Date target_date_obj = inputFormat.parse(targetDate);

                                if (target_date_obj.before(today)) {
                                    goalDateText.setTextColor(this.getColor(R.color.red));
                                } else {
                                    goalDateText.setTextColor(this.getColor(R.color.cerulean));
                                }
                            } else {
                                goalDateText.setTextColor(this.getColor(R.color.green));
                            }


                            NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
                            progressText.setText(String.format("%s of %s", currency.format(accountBalance), currency.format(amount)));

                            int progress = (int) ((accountBalance / amount) * 100);
                            if (progress > 100) progress = 100;
                            progressBar.setProgress(progress);

                            if (accountBalance >= amount) {
                                progressBar.setProgressTintList(getResources().getColorStateList(R.color.green, null));
                            } else {
                                progressBar.setProgressTintList(getResources().getColorStateList(R.color.cerulean, null));
                            }

                        } else {
                            Toast.makeText(ViewGoal.this, "Failed to fetch goal details", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(ViewGoal.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(ViewGoal.this, "Network Error. Check your connection!", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("goalID", goal_id);
                params.put("userID", user_id);
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }
    private void showDeleteConfirmationDialog(String goalID) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete this goal?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    deleteGoal(goalID);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void showCompleteConfirmationDialog(String goalID) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Confirm Completion")
                .setMessage("Are you sure you want to complete this goal?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    completeGoal(goalID);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void completeGoal(String goalID) {
        StringRequest request = new StringRequest(Request.Method.POST, completeGoalURL,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getString("status").equals("success")) {
                            Toast.makeText(this, "Goal complete!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ViewGoal.this, Dashboard.class);
                            intent.putExtra("fragmentToOpen", "Goals");
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(ViewGoal.this, "Failed to complete goal.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("JSONError", "Parsing error: " + e.getMessage());
                        Toast.makeText(ViewGoal.this, "Error parsing response.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("VolleyError", "Request error: " + error.toString());
                    Toast.makeText(ViewGoal.this, "Error complete goal.", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("goalID", goalID);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
    private void deleteGoal(String goalID) {
        StringRequest request = new StringRequest(Request.Method.POST, deleteGoalURL,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getString("status").equals("success")) {
                            Toast.makeText(this, "Goal deleted successfully!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ViewGoal.this, Dashboard.class);
                            intent.putExtra("fragmentToOpen", "Goals");
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(ViewGoal.this, "Failed to delete goal.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("JSONError", "Parsing error: " + e.getMessage());
                        Toast.makeText(ViewGoal.this, "Error parsing response.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("VolleyError", "Request error: " + error.toString());
                    Toast.makeText(ViewGoal.this, "Error deleting goal.", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("goalID", goalID);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}