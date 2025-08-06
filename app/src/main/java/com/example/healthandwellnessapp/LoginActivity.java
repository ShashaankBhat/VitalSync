package com.example.healthandwellnessapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private ProgressBar progressBar;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ImageButton backButton = findViewById(R.id.back_button);
        Button loginButton = findViewById(R.id.login_button);
        TextView signupPrompt = findViewById(R.id.signup_prompt);

        emailInput = findViewById(R.id.editTextInput1);
        passwordInput = findViewById(R.id.editTextInput2);
        progressBar = findViewById(R.id.progress_bar);

        db = new DatabaseHelper(this);

        backButton.setOnClickListener(v -> {
            Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(mainIntent);
            finish();
        });

        signupPrompt.setOnClickListener(v -> {
            Intent signupIntent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(signupIntent);
        });

        loginButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                progressBar.setVisibility(View.VISIBLE);

                new Thread(() -> {
                    boolean isAuthenticated = db.authenticateUser(email, password);

                    runOnUiThread(() -> progressBar.setVisibility(View.GONE));

                    if (isAuthenticated) {
                        SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("email", email);
                        editor.apply();

                        Intent dashboardIntent = new Intent(LoginActivity.this, DashboardActivity.class);
                        startActivity(dashboardIntent);
                        finish();
                    } else {
                        runOnUiThread(() -> {
                            CustomDialog dialog = new CustomDialog(LoginActivity.this, "Invalid email or password");
                            dialog.setCloseButtonClickListener(v1 -> {});
                            dialog.show();
                        });
                    }
                }).start();
            }
        });
    }
}
