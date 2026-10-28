package com.example.ubicomplab3;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MotionService extends Service implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor motionManager;
    private Handler handler;

    private static final String CHANNEL_ID = "10002";

    @Override
    public void onCreate() {
        super.onCreate();

        handler = new Handler(); // thread communication handler

        // get sensor service
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        motionManager = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // register sensor listener
        if (motionManager != null) {
            mSensorManager.registerListener(this, motionManager, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int id) {
        return START_STICKY; // keeps service running
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        // movement detection threshold
        double accelerationSq = (x * x) + (y * y) + (z * z);
        double acceleration = Math.sqrt(accelerationSq);

        // if movement exceeds baseline gravity
        if (Math.abs(acceleration - 9.81) > 2.0) {
            String timestamp = "Motion detected at: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

            // create background thread
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    handler.post(new Runnable() { // handler posts task back to main
                        @Override
                        public void run() {
                            triggerNotification(timestamp);
                        }
                    });
                }
            });
            t.start();
        }
    }

    // required for SensorEventListener interface
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void triggerNotification(String timeText) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Motion Channel", NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("Motion alert channel.");
        notificationManager.createNotificationChannel(channel);

        // intent triggered when notification is selected
        Intent intent = new Intent(this, MotionDetailActivity.class);
        intent.putExtra("motion_time", timeText);

        PendingIntent pIntent = PendingIntent.getActivity(
                this, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Phone Movement Detected!")
                .setContentText(timeText)
                .setSmallIcon(R.drawable. ic_launcher_foreground )
                .setAutoCancel(true)
                .setContentIntent(pIntent);

        notificationManager.notify(1, builder.build());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
    }
}