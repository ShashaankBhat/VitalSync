package com.example.healthandwellnessapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {

    private EditText nameInput, emailInput, passwordInput, phoneInput;
    private ProgressBar progressBar;
    private DatabaseHelper db;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        ImageButton backButton = findViewById(R.id.back_button_signup);
        Button signupButton = findViewById(R.id.signup_button);
        TextView loginPrompt = findViewById(R.id.login_prompt);

        nameInput = findViewById(R.id.name_input);
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        phoneInput = findViewById(R.id.phone_input);
        progressBar = findViewById(R.id.progressBar);
        db = new DatabaseHelper(this);

        progressBar.setVisibility(View.GONE);

        backButton.setOnClickListener(v -> {
            Intent mainIntent = new Intent(SignUpActivity.this, MainActivity.class);
            startActivity(mainIntent);
            finish();
        });

        loginPrompt.setOnClickListener(v -> {
            Intent loginIntent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(loginIntent);
        });

        signupButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String phone = phoneInput.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty()) {
                Toast.makeText(SignUpActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return; // Return to prevent further execution
            }

            if (!EMAIL_PATTERN.matcher(email).matches()) {
                Toast.makeText(SignUpActivity.this, "Invalid email format", Toast.LENGTH_SHORT).show();
                return; // Return to prevent further execution
            }

            progressBar.setVisibility(View.VISIBLE);

            new Thread(() -> {
                boolean isInserted = db.insertUser(name, email, password, phone);

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);

                    CustomDialog dialog;
                    if (isInserted) {
                        dialog = new CustomDialog(SignUpActivity.this, "User Registered Successfully");
                        dialog.setCloseButtonClickListener(v1 -> {
                            Intent loginIntent = new Intent(SignUpActivity.this, LoginActivity.class);
                            startActivity(loginIntent);
                            finish();
                        });
                    } else {
                        dialog = new CustomDialog(SignUpActivity.this, "Email already exists. Please use a different email.");
                        dialog.setCloseButtonClickListener(v1 -> {
                            // Optional: Clear input fields on error
                            emailInput.setText("");
                            passwordInput.setText("");
                            phoneInput.setText("");
                            nameInput.setText("");
                        });
                    }
                    dialog.show();
                });
            }).start();
        });

    }
}
