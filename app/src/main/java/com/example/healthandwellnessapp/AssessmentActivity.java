package com.example.healthandwellnessapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class AssessmentActivity extends AppCompatActivity {

    private EditText etWeight, etHeight, etBmi;
    private DatabaseHelper databaseHelper;
    private LineChart lineChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assessment);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_assessment);
        bottomNavigationView.setOnNavigationItemSelectedListener(this::onNavigationItemSelected);

        etWeight = findViewById(R.id.et_weight);
        etHeight = findViewById(R.id.et_height);
        etBmi = findViewById(R.id.et_bmi);
        Button btnSave = findViewById(R.id.btn_save_assessment);

        databaseHelper = new DatabaseHelper(this);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAssessmentData();
            }
        });

        lineChart = findViewById(R.id.assessment_graph);
        setupLineChart();
        loadChartData();

        TextView tvAssessmentDate = findViewById(R.id.tv_assessment_date);

        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        tvAssessmentDate.setText("Assessment Date: " + currentDate);
    }

    private void saveAssessmentData() {
        String weight = etWeight.getText().toString().trim();
        String height = etHeight.getText().toString().trim();
        String bmi = etBmi.getText().toString().trim();

        if (weight.isEmpty() || height.isEmpty() || bmi.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isInserted = databaseHelper.insertAssessmentData(weight, height, bmi);

        if (isInserted) {
            Toast.makeText(this, "Assessment data saved successfully", Toast.LENGTH_SHORT).show();
            loadChartData(); // Refresh the chart after data is saved
        } else {
            Toast.makeText(this, "Error saving data", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupLineChart() {
        lineChart.getDescription().setEnabled(false);

        // Customize X-axis
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        // Customize Y-axis
        YAxis leftAxis = lineChart.getAxisLeft();
        lineChart.getAxisRight().setEnabled(false);  // Disable right Y-axis
    }

    private void loadChartData() {
        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> xLabels = new ArrayList<>();

        Cursor cursor = databaseHelper.getAllAssessments();
        if (cursor.moveToFirst()) {
            int index = 0;
            do {
                @SuppressLint("Range")
                float bmi = cursor.getFloat(cursor.getColumnIndex("bmi"));
                @SuppressLint("Range")
                long timestamp = cursor.getLong(cursor.getColumnIndex("timestamp"));

                entries.add(new Entry(index, bmi));
                xLabels.add(convertTimestampToDate(timestamp)); // Convert timestamp to date for X-axis labels
                index++;
            } while (cursor.moveToNext());
        }
        cursor.close();

        LineDataSet dataSet = new LineDataSet(entries, "BMI Progress");
        dataSet.setColor(getResources().getColor(R.color.orangeShade1));
        dataSet.setLineWidth(2f);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.invalidate(); // Refresh the chart
    }

    private String convertTimestampToDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    @SuppressLint("NonConstantResourceId")
    private boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_home:
                startActivity(new Intent(this, DashboardActivity.class));
                overridePendingTransition(0, 0);
                return true;
            case R.id.nav_assessment:
                return true;
            case R.id.nav_profile:
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            case R.id.nav_logout:
                logout();
                return true;
        }
        return false;
    }

    private void logout() {
        SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("email");
        editor.apply();

        Intent intent = new Intent(AssessmentActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
