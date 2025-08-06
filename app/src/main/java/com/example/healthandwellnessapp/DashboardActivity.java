package com.example.healthandwellnessapp;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DashboardActivity extends AppCompatActivity implements NutritionVideosAdapter.OnVideoClickListener, SensorEventListener {

    private DatabaseHelper databaseHelper;
    private RecyclerView nutritionVideosRecyclerView;
    private NutritionVideosAdapter nutritionVideosAdapter;
    private List<Video> videoList;
    private ViewPager2 guidelinesViewPager;
    private GuidelinesAdapter guidelinesAdapter;
    private List<String> guidelines;
    private TabLayout tabLayout;

    private SensorManager sensorManager;
    private Sensor stepCounterSensor;

    private int stepCount = 0;
    private int lastStepCountReading = 0;

    private TextView stepCountText;
    private TextView caloriesBurnedText;
    private TextView distanceCoveredText;
    private CircularProgressIndicator circularProgressIndicator;
    private SharedPreferences sharedPreferences;

    private static final String PREFS_NAME = "HealthMatePrefs";
    private static final String STEP_COUNT_KEY = "stepCount";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        databaseHelper = new DatabaseHelper(this);
        guidelinesViewPager = findViewById(R.id.guidelines_viewpager);
        tabLayout = findViewById(R.id.tab_layout);

        guidelines = databaseHelper.getAllGuidelines();
        if (guidelines.isEmpty()) {
            Toast.makeText(this, "No guidelines available.", Toast.LENGTH_SHORT).show();
        } else {
            guidelinesAdapter = new GuidelinesAdapter(guidelines);
            guidelinesViewPager.setAdapter(guidelinesAdapter);

            new TabLayoutMediator(tabLayout, guidelinesViewPager, (tab, position) -> {
                tab.setCustomView(R.layout.custom_tab);
            }).attach();
        }

        nutritionVideosRecyclerView = findViewById(R.id.nutrition_videos_recyclerview);
        nutritionVideosRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        videoList = new ArrayList<>();
        nutritionVideosAdapter = new NutritionVideosAdapter(videoList, this);
        nutritionVideosRecyclerView.setAdapter(nutritionVideosAdapter);

        fetchVideos();

        TextView userGreeting = findViewById(R.id.user_greeting);
        String userEmail = getCurrentUserEmail();
        String userName = databaseHelper.getUserName(userEmail);
        userGreeting.setText("Hey " + (userName != null ? userName : "User") + ",");

        Button bmiButton = findViewById(R.id.bmi_button);
        bmiButton.setOnClickListener(v -> {
            Intent bmiIntent = new Intent(DashboardActivity.this, BMIActivity.class);
            startActivity(bmiIntent);
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this::onNavigationItemSelected);

        // Step Counter UI Elements
        stepCountText = findViewById(R.id.progressText);
        caloriesBurnedText = findViewById(R.id.calories_burned);
        distanceCoveredText = findViewById(R.id.distance_covered);

        circularProgressIndicator = findViewById(R.id.circularProgressIndicator);
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        loadStepCount();
        updateStepCount();

        scheduleMidnightReset();
    }

    private void fetchVideos() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://healthandwellness-875d3-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("videos");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                videoList.clear(); // Clear the list before adding new items
                for (DataSnapshot videoSnapshot : dataSnapshot.getChildren()) {
                    Video video = videoSnapshot.getValue(Video.class);
                    if (video != null) {
                        videoList.add(video);
                        Log.i("Video Added", "Video Title: " + video.getTitle()); // Debug log with video title
                    }
                }
                nutritionVideosAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
                Log.e("FirebaseError", "Failed to read videos", databaseError.toException());
                Toast.makeText(DashboardActivity.this, "Failed to load videos.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("NonConstantResourceId")
    private boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_home:
                return true;
            case R.id.nav_assessment:
                startActivity(new Intent(this, AssessmentActivity.class));
                overridePendingTransition(0, 0);
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

        Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            if (lastStepCountReading == 0) {
                lastStepCountReading = (int) event.values[0];
            }
            stepCount = (int) event.values[0] - lastStepCountReading;
            updateStepCount();
        }
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void updateStepCount() {
        stepCountText.setText(String.valueOf(stepCount));
        int caloriesBurned = (int) (stepCount * 0.04);
        caloriesBurnedText.setText("Calories Burned: " + caloriesBurned);

        float distanceCovered = stepCount * 0.6f;
        distanceCoveredText.setText(String.format("Distance Covered: %.2f m", distanceCovered));

        circularProgressIndicator.setMax(1000);
        circularProgressIndicator.setProgress(stepCount);
        saveStepCount();
    }

    private void loadStepCount() {
        stepCount = sharedPreferences.getInt(STEP_COUNT_KEY, 0);
        lastStepCountReading = sharedPreferences.getInt("lastStepCountReading", 0);
    }

    private void saveStepCount() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(STEP_COUNT_KEY, stepCount);
        editor.putInt("lastStepCountReading", lastStepCountReading);
        editor.apply();
    }

    private void scheduleMidnightReset() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, MidnightResetReceiver.class);
        intent.setAction("com.example.healthandwellnessapp.RESET_ACTION");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 19);
        calendar.set(Calendar.MINUTE, 57);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (System.currentTimeMillis() > calendar.getTimeInMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }

    private final BroadcastReceiver stepCountResetReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadStepCount();
            updateStepCount();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);
        ContextCompat.registerReceiver(
                this,
                stepCountResetReceiver,
                new IntentFilter("com.example.healthandwellnessapp.STEP_COUNT_RESET"),
                ContextCompat.RECEIVER_NOT_EXPORTED
        );
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        unregisterReceiver(stepCountResetReceiver);
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }

    private String getCurrentUserEmail() {
        SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return preferences.getString("email", null);
    }

    @Override
    public void onVideoClick(String videoUrl) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl));
        startActivity(intent);
    }
}
