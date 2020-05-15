package com.e.facex;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.TimerTask;

public class RotateCheck extends TimerTask {

    private SensorManager msensorManager;
    private SensorEventListener sensorEventListener;
    private float[] rotationMatrix;
    private float[] accelData;
    private float[] magnetData;
    private float[] OrientationData;
    private float axis;
    Context contex;

    public RotateCheck(Context context){
        this.contex = context;
    }

    @Override
    public void run() {
        msensorManager = (SensorManager)contex.getSystemService(Context.SENSOR_SERVICE);
        rotationMatrix = new float[16];
        accelData = new float[3];
        magnetData = new float[3];
        OrientationData = new float[3];

        sensorEventListener = new SensorEventListener() {
            @Override
            public void onAccuracyChanged(Sensor arg0, int arg1) {
            }

            @Override
            public void onSensorChanged(SensorEvent event) {
                loadNewSensorData(event);
                SensorManager.getRotationMatrix(rotationMatrix, null, accelData, magnetData); //Получаем матрицу поворота
                SensorManager.getOrientation(rotationMatrix, OrientationData);
                //Log.d("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", String.valueOf(OrientationData[1]));
            }
        };
        msensorManager.registerListener(sensorEventListener, msensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
        msensorManager.registerListener(sensorEventListener, msensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);

        axis = Math.round(Math.toDegrees(OrientationData[1]));

        msensorManager.unregisterListener(sensorEventListener);

        Log.d("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", String.valueOf(axis));
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