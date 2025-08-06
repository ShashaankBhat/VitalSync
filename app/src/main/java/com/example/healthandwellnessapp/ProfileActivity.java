// ProfileActivity.java
package com.example.healthandwellnessapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.ByteArrayOutputStream;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    private ImageView profileImageView;
    private DatabaseHelper db;
    private ExecutorService executorService;

    private TextView profileName, profilePhone, profileHeight, profileWeight;
    private EditText editHeight, editWeight;
    private Button btnEditProfile, btnCallDietician, btnCallEmergency;
    private boolean isEditing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        executorService = Executors.newSingleThreadExecutor();
        db = new DatabaseHelper(this);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);
        bottomNavigationView.setOnNavigationItemSelectedListener(this::onNavigationItemSelected);

        TextView profileName = findViewById(R.id.profile_name);
        String userEmail = getCurrentUserEmail();

        String userName = db.getUserName(userEmail);
        profileName.setText(userName != null ? userName : "User");

        TextView profilePhone = findViewById(R.id.profile_phone);

        String userPhone = db.getPhoneNumber(userEmail);
        profilePhone.setText(userPhone != null ? userPhone : "Phone");

        profileImageView = findViewById(R.id.profile_image);
        db = new DatabaseHelper(this);

        loadProfileImage();

        profileHeight = findViewById(R.id.profile_height);
        profileWeight = findViewById(R.id.profile_weight);
        editHeight = findViewById(R.id.edit_height);
        editWeight = findViewById(R.id.edit_weight);

        editHeight.setVisibility(View.GONE);
        editWeight.setVisibility(View.GONE);

        btnEditProfile = findViewById(R.id.btn_edit_profile);
        btnCallDietician = findViewById(R.id.btn_call_dietician);
        btnCallEmergency = findViewById(R.id.btn_call_emergency);

        btnEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleEditMode();
            }
        });

        btnCallDietician = findViewById(R.id.btn_call_dietician);
        btnCallEmergency = findViewById(R.id.btn_call_emergency);

        btnCallDietician.setOnClickListener(v -> openDialer("9315672082"));

        btnCallEmergency.setOnClickListener(v -> makeEmergencyCall("9315672082"));
    }

    private void openDialer(String phoneNumber) {
        Intent dialIntent = new Intent(Intent.ACTION_DIAL);
        dialIntent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(dialIntent);
    }

    private void makeEmergencyCall(String phoneNumber) {
        if (checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(intent);
        } else {
            Toast.makeText(this, "Permission to make calls is required", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleEditMode() {
        if (isEditing) {
            String newHeight = editHeight.getText().toString();
            String newWeight = editWeight.getText().toString();

            profileHeight.setText("Height: " + newHeight + " cm");
            profileWeight.setText("Weight: " + newWeight + " kg");

            editHeight.setVisibility(View.GONE);
            editWeight.setVisibility(View.GONE);
            profileHeight.setVisibility(View.VISIBLE);
            profileWeight.setVisibility(View.VISIBLE);

            btnEditProfile.setText("Edit Profile");
        } else {
            String currentHeight = profileHeight.getText().toString().replace("Height: ", "").replace(" cm", "");
            String currentWeight = profileWeight.getText().toString().replace("Weight: ", "").replace(" kg", "");

            editHeight.setText(currentHeight);
            editWeight.setText(currentWeight);

            profileHeight.setVisibility(View.GONE);
            profileWeight.setVisibility(View.GONE);
            editHeight.setVisibility(View.VISIBLE);
            editWeight.setVisibility(View.VISIBLE);

            btnEditProfile.setText("Save");
        }
        isEditing = !isEditing;
    }

    private String getCurrentUserEmail() {
        SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return preferences.getString("email", null);
    }

    public void selectProfileImage(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    private void loadProfileImage() {
        executorService.execute(() -> {
            byte[] imageBytes = db.getProfileImage();
            Bitmap bitmap;
            if (imageBytes != null) {
                bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            } else {
                bitmap = null;
            }
            // Update the UI on the main thread
            runOnUiThread(() -> {
                if (bitmap != null) {
                    profileImageView.setImageBitmap(bitmap);
                }
            });
        });
    }

    private void onImageSelected(byte[] imageBytes) {
        executorService.execute(() -> {
            db.updateProfileImage(imageBytes);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                profileImageView.setImageBitmap(bitmap);

                // Convert to byte array and store in database
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] imageBytes = stream.toByteArray();

                // Store image in the database
                onImageSelected(imageBytes);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @SuppressLint("NonConstantResourceId")
    private boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_home:
                startActivity(new Intent(this, DashboardActivity.class));
                overridePendingTransition(0, 0);
                return true;
            case R.id.nav_assessment:
                startActivity(new Intent(this, AssessmentActivity.class));
                overridePendingTransition(0, 0);
                return true;
            case R.id.nav_profile:
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

        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}
