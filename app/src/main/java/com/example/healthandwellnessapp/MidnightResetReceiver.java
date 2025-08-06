package com.example.healthandwellnessapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class MidnightResetReceiver extends BroadcastReceiver {
    private static final String PREFS_NAME = "HealthMatePrefs";
    private static final String STEP_COUNT_KEY = "stepCount";

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(STEP_COUNT_KEY, 0);
        editor.putInt("lastStepCountReading", 0); // Reset last step count reading
        editor.apply();

        // Log the reset
        Log.d("MidnightResetReceiver", "Step count and last step count reading reset to 0.");

        // Notify the activity
        Intent resetIntent = new Intent("com.example.healthandwellnessapp.STEP_COUNT_RESET");
        context.sendBroadcast(resetIntent);
    }
}
