package com.e.facex;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LongService extends Service implements SensorEventListener {

    String TAG = "LONGSERVICE";
    ScreenOnReceiver screenOnReceiver;
    IntentFilter screenStateFilter;
    ScheduledExecutorService executor;

    private SensorManager msensorManager; //Менеджер сенсоров аппрата

    private float[] rotationMatrix;     //Матрица поворота
    private float[] accelData;           //Данные с акселерометра
    private float[] magnetData;       //Данные геомагнитного датчика
    private float[] OrientationData; //Матрица положения в пространстве

    Object[] confvalues = new Object[5];
    Filer filer = new Filer(this);
    Float period;

    float rounddata;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: ");
        return null;
    }

    public void onCreate(){
        super.onCreate();
        Log.d(TAG, "onCreate: ");

        startInForeground();
        screenOnReceiver = new ScreenOnReceiver();
        screenStateFilter = new IntentFilter("LOCKER_SCREEN");
        screenStateFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenStateFilter.addAction(Intent.ACTION_SCREEN_OFF);
        screenStateFilter.addAction(Intent.ACTION_USER_PRESENT);
        screenStateFilter.addAction("StartBackServiceLoop");
        this.registerReceiver(screenOnReceiver, screenStateFilter);

        msensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);

        rotationMatrix = new float[16];
        accelData = new float[3];
        magnetData = new float[3];
        OrientationData = new float[3];

        filer.InitFile();
        confvalues = filer.GetValues();
        period = Float.valueOf(String.valueOf(confvalues[2]));

        msensorManager.registerListener(this, msensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI );
        msensorManager.registerListener(this, msensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_UI );

        Runnable runnable = new Runnable() {
            public void run() {
                Log.d("AXAXAXAXAXAXAXAX", "Rotate: " + rounddata);
                if (rounddata < -15){
                    Intent intent = new Intent("StartBackServiceLoop");
                    sendBroadcast(intent);
                }
            }
        };

        executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(runnable, 0, period.intValue(), TimeUnit.MILLISECONDS);
    }

    public int onStartCommand(Intent intent, int flags, int startId){
        Log.d(TAG, "onStart: ");
        return START_NOT_STICKY;
    }

    public void onDestroy(){
        Log.d(TAG, "onDestroy: ");
        msensorManager.unregisterListener(this);
        executor.shutdown();
        this.unregisterReceiver(screenOnReceiver);
        stopForeground(STOP_FOREGROUND_REMOVE);
        super.onDestroy();
    }

    private void startInForeground() {
        Intent notificationIntent = new Intent(this, LongService.class);
        PendingIntent pendingIntent=PendingIntent.getActivity(this,0,notificationIntent,0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "4023")
                .setSmallIcon(R.drawable.icon)
                .setContentTitle("FaceX")
                .setContentText("Recognition is on!")
                .setTicker("TICKER")
                .setContentIntent(pendingIntent);
        Notification notification=builder.build();
        if(Build.VERSION.SDK_INT>=26) {
            NotificationChannel channel = new NotificationChannel("4023", "Channelol", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Its my notification");
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
        startForeground(5554023, notification);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        loadNewSensorData(event); // Получаем данные с датчика
        SensorManager.getRotationMatrix(rotationMatrix, null, accelData, magnetData); //Получаем матрицу поворота
        SensorManager.getOrientation(rotationMatrix, OrientationData); //Получаем данные ориентации устройства в пространстве
        rounddata = Math.round(Math.toDegrees(OrientationData[1]));
        //Log.d("AAAAAAAAAAAAAAAAAAAAAB", "Rotate: " + String.valueOf(Math.round(Math.toDegrees(OrientationData[1]))));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void loadNewSensorData(SensorEvent event) {
        final int type = event.sensor.getType(); //Определяем тип датчика
        if (type == Sensor.TYPE_ACCELEROMETER) { //Если акселерометр
            accelData = event.values.clone();
        }

        if (type == Sensor.TYPE_MAGNETIC_FIELD) { //Если геомагнитный датчик
            magnetData = event.values.clone();
        }
    }
}
