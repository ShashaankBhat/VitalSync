package com.example.healthandwellnessapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class BMIActivity extends AppCompatActivity {

    private EditText heightInput, weightInput;
    private TextView bmiResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bmi);

        heightInput = findViewById(R.id.height_input);
        weightInput = findViewById(R.id.weight_input);
        bmiResult = findViewById(R.id.bmi_result);
        Button calculateButton = findViewById(R.id.calculate_button);

        calculateButton.setOnClickListener(v -> calculateBMI());

        ImageButton backBtn = findViewById(R.id.back_button_bmi);

        backBtn.setOnClickListener(v -> {
            Intent backIntent = new Intent(BMIActivity.this, DashboardActivity.class);
            startActivity(backIntent);
            finish();
        });
    }

    @SuppressLint("DefaultLocale")
    private void calculateBMI() {
        String heightStr = heightInput.getText().toString().trim();
        String weightStr = weightInput.getText().toString().trim();

        if (heightStr.isEmpty() || weightStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double height = Double.parseDouble(heightStr) / 100;
        double weight = Double.parseDouble(weightStr);
        double bmi = weight / (height * height);

        String healthStatus = getHealthStatus(bmi);

        bmiResult.setText(String.format("Your BMI: %.2f\nStatus: %s", bmi, healthStatus));
    }

    private String getHealthStatus(double bmi) {
        if (bmi < 18.5) {
            return "Underweight";
        } else if (bmi >= 18.5 && bmi < 24.9) {
            return "Normal weight";
        } else if (bmi >= 25 && bmi < 29.9) {
            return "Overweight";
        } else {
            return "Obesity";
        }
    }
}
