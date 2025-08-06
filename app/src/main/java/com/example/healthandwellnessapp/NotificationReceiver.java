package com.example.healthandwellnessapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.Manifest;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.util.Random;

public class NotificationReceiver extends BroadcastReceiver {
    private final String[] healthFacts = {
            "Stay hydrated by drinking at least 8 glasses of water daily.",
            "Regular physical activity can improve your mental health.",
            "Eating fruits and vegetables can help reduce the risk of chronic diseases.",
            "Sleep is crucial for overall health and well-being.",
            "Maintaining a healthy weight can improve your health and quality of life."
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("NotificationReceiver", "onReceive triggered");

        Random random = new Random();
        String randomFact = healthFacts[random.nextInt(healthFacts.length)];

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "healthmate_channel")
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("Health Reminder")
                .setContentText(randomFact)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(200, builder.build());
    }
}

