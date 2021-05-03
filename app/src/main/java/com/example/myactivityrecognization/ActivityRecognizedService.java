package com.example.myactivityrecognization;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;
import java.util.List;

public class ActivityRecognizedService extends IntentService implements SensorEventListener {

    private static final String TAG = "ActivityRecognizedSer";
    public static boolean isLightDark;
    private LocalBroadcastManager mLocalBroadcastManager;
    private SensorManager sensorManager;
    private Sensor lightSensor;
    private boolean isLightSensorRegistered;

    public ActivityRecognizedService() {
        super("ActivityRecognizedSer");
    }

    public ActivityRecognizedService(String name) {
        super(name);
    }
    @Override
    public void onCreate() {
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, "onHandleIntent: ");
        sensorManager=(SensorManager) getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            handleDetectedActivities(result.getProbableActivities());
        }
    }

    //a list of activities is passed
    private void handleDetectedActivities(List<DetectedActivity> probableActivities) {
        for (DetectedActivity activity : probableActivities) {
            switch (activity.getType()) {
                case DetectedActivity.IN_VEHICLE: {
                    Log.d("ActivityRecognition", "In Vehicle: " + activity.getConfidence());
                    sendMessage("In Vehicle", activity.getConfidence());
                    break;
                }
                case DetectedActivity.ON_BICYCLE: {
                    Log.d("ActivityRecognition", "On Bicycle: " + activity.getConfidence());
                    sendMessage("On Bicycle", activity.getConfidence());
                    break;
                }
                case DetectedActivity.ON_FOOT: {
                    Log.d("ActivityRecognition", "On Foot: " + activity.getConfidence());
                    sendMessage("On Foot", activity.getConfidence());
                    break;
                }
                case DetectedActivity.RUNNING: {
                    Log.d("ActivityRecognition", "Running: " + activity.getConfidence());
                    sendMessage("Running", activity.getConfidence());
                    break;
                }
                case DetectedActivity.STILL: {
                    Log.d("ActivityRecognition", "Still: " + activity.getConfidence());
                    sendMessage("Still", activity.getConfidence());
                    break;
                }
                case DetectedActivity.TILTING: {
                    Log.d("ActivityRecognition", "Tilting: " + activity.getConfidence());
                    sendMessage("Tilting", activity.getConfidence());
                    break;
                }
                case DetectedActivity.WALKING: {
                    Log.d("ActivityRecognition", "Walking: " + activity.getConfidence());
                    sendMessage("Walking", activity.getConfidence());
                    break;
                }
                case DetectedActivity.UNKNOWN: {
                    Log.e("ActivityRecognition", "Unknown: " + activity.getConfidence());
                    break;
                }
            }
        }
    }

    private void sendMessage(String message, int confidence) {
        // send message only if the confidence level is above 75
        //check if the activity is still if yes then take the light sensor value
        if(lightSensor!=null){
            sensorManager.registerListener(this,lightSensor,SensorManager.SENSOR_DELAY_FASTEST);
            isLightSensorRegistered=true;
            Log.d(TAG, "sendMessage: light sensor registered");
        }else {
            Log.d(TAG, "sendMessage: light sensor is null");
        }
        Intent intent = new Intent(Constants.INTENT_FILTER);
        if (confidence >= 75) {
            Log.d(TAG, "sendMessage: " + message + confidence);
            intent.putExtra(Constants.MESSAGE_KEY, message);
            intent.putExtra(Constants.CONFIDENCE_KEY, confidence);
            intent.putExtra(Constants.LIGHT_KEY, isLightDark);

            //first light sensor registered then records a value and then unregistered we have a light value it means now we can send the broadcast
            mLocalBroadcastManager.sendBroadcast(intent);
            Log.d(TAG, "sendMessage: broadcast sent");

        }
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d(TAG, "onSensorChanged: inside on sensor changed");
        if (Sensor.TYPE_LIGHT == event.sensor.getType()) {
            if (event.values[0] < 2)
                isLightDark = true;
            if(lightSensor!=null) {
                sensorManager.unregisterListener(this);
                isLightSensorRegistered=false;
                Log.d(TAG, "onSensorChanged: light sensor unregistered");
            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { /*on accuracy changed*/}
}
