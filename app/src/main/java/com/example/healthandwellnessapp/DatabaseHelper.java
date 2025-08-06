package com.example.healthandwellnessapp;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "users.db"; // Use users.db
    private static final String USERS_TABLE_NAME = "users";
    private static final String GUIDELINES_TABLE_NAME = "dietary_guidelines";

    private static final String COL_2 = "email";
    private static final String COL_3 = "password";
    private static final String COL_4 = "name";
    private static final String COL_5 = "phone";
    private static final String COL_6 = "profile_image";

    private static final String GUIDELINES_COL_1 = "id";
    private static final String GUIDELINES_COL_2 = "guideline";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 5);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + USERS_TABLE_NAME + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "email TEXT NOT NULL UNIQUE," +
                "phone TEXT NOT NULL," +
                "password TEXT NOT NULL," +
                "profile_image BLOB" +
                ")";
        db.execSQL(CREATE_USERS_TABLE);

        String CREATE_GUIDELINES_TABLE = "CREATE TABLE " + GUIDELINES_TABLE_NAME + " (" +
                GUIDELINES_COL_1 + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                GUIDELINES_COL_2 + " TEXT NOT NULL" +
                ")";
        db.execSQL(CREATE_GUIDELINES_TABLE);

        String CREATE_ASSESSMENT_TABLE = "CREATE TABLE assessments (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "weight TEXT," +
                "height TEXT," +
                "bmi TEXT," +
                "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)";
        db.execSQL(CREATE_ASSESSMENT_TABLE);

        insertDefaultGuidelines(db);
    }

    public boolean insertAssessmentData(String weight, String height, String bmi) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("weight", weight);
        contentValues.put("height", height);
        contentValues.put("bmi", bmi);

        long result = db.insert("assessments", null, contentValues);
        return result != -1;
    }

    public Cursor getAllAssessments() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT weight, height, bmi, timestamp FROM assessments", null);
    }

    private void insertDefaultGuidelines(SQLiteDatabase db) {
        String[] guidelines = {
                "Eat a variety of foods to ensure balanced nutrition.",
                "Include plenty of fruits and vegetables in your daily meals.",
                "Limit the intake of sugar, salt, and saturated fats.",
                "Drink at least 8 glasses of water every day.",
                "Avoid skipping meals, especially breakfast.",
                "Practice portion control to maintain a healthy weight.",
                "Incorporate whole grains into your diet.",
                "Opt for lean protein sources such as fish, chicken, and legumes.",
                "Exercise regularly to complement your diet.",
                "Avoid processed foods and prefer home-cooked meals."
        };

        ContentValues contentValues = new ContentValues();
        for (String guideline : guidelines) {
            contentValues.put(GUIDELINES_COL_2, guideline);
            db.insert(GUIDELINES_TABLE_NAME, null, contentValues);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + USERS_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + GUIDELINES_TABLE_NAME);
        onCreate(db);
    }

    public List<String> getAllGuidelines() {
        List<String> guidelinesList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + GUIDELINES_COL_2 + " FROM " + GUIDELINES_TABLE_NAME, null);

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String guideline = cursor.getString(cursor.getColumnIndex(GUIDELINES_COL_2));
                guidelinesList.add(guideline);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return guidelinesList;
    }


    @SuppressLint("Range")
    public String getUserName(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COL_4 + " FROM " + USERS_TABLE_NAME + " WHERE " + COL_2 + " = ?", new String[]{email});

        String name = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                name = cursor.getString(cursor.getColumnIndex(COL_4));
            }
            cursor.close();
        }

        return name;
    }

    @SuppressLint("Range")
    public String getPhoneNumber(String email){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COL_5 + " FROM " + USERS_TABLE_NAME + " WHERE " + COL_2 + " = ?", new String[]{email});

        String phone = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                phone = cursor.getString(cursor.getColumnIndex(COL_5));
            }
            cursor.close();
        }

        return phone;
    }

    public boolean insertUser(String name, String email, String password, String phone) {
        if (emailExists(email)) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_4, name);
        contentValues.put(COL_2, email);
        contentValues.put(COL_3, password);
        contentValues.put(COL_5, phone);
        long result = db.insert(USERS_TABLE_NAME, null, contentValues);
        return result != -1;
    }

    public boolean emailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + USERS_TABLE_NAME + " WHERE email = ?", new String[]{email});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public boolean authenticateUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + USERS_TABLE_NAME + " WHERE email = ? AND password = ?", new String[]{email, password});
        boolean isAuthenticated = cursor.getCount() > 0;
        cursor.close();
        return isAuthenticated;
    }

    public void updateProfileImage(byte[] imageBytes) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_6, imageBytes);

        String email = "user@example.com";
        db.update(USERS_TABLE_NAME, contentValues, COL_2 + " = ?", new String[]{email});
    }

    @SuppressLint("Range")
    public byte[] getProfileImage() {
        SQLiteDatabase db = this.getReadableDatabase();
        String email = "user@example.com";  // Retrieve the correct email from session/login
        Cursor cursor = db.rawQuery("SELECT " + COL_6 + " FROM " + USERS_TABLE_NAME + " WHERE " + COL_2 + " = ?", new String[]{email});

        byte[] imageBytes = null;
        if (cursor != null && cursor.moveToFirst()) {
            imageBytes = cursor.getBlob(cursor.getColumnIndex(COL_6));
            cursor.close();
        }
        return imageBytes;
    }

}
