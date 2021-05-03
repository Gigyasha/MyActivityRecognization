package com.example.myactivityrecognization;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MainActivity";
    private TextView mActivityType;
    private TextView mConfidenceLevel;
    private TextView mStatus;
    private Button mStartBtn;
    private Button mStopBtn;
    private static final long UPDATE_INTERVAL = 6000;
    private GoogleApiClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //creating google api client object
        mClient = new GoogleApiClient.Builder(this)
                .addApiIfAvailable(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
//        mClient.connect();

        mStartBtn = (Button) findViewById(R.id.startBtn);
        mStopBtn = (Button) findViewById(R.id.stopBtn);
        mActivityType = (TextView) findViewById(R.id.activityType);
        mConfidenceLevel = (TextView) findViewById(R.id.confidence);
        mStatus = (TextView) findViewById(R.id.status);
        mStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mClient.disconnect();
                LocalBroadcastManager.getInstance(MainActivity.this).unregisterReceiver(mBroadcastReceiver);
                mStatus.setText("Status: stopped");
                Toast.makeText(MainActivity.this, "Stopped", Toast.LENGTH_SHORT).show();

            }
        });

        mStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mClient.connect();
                LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.INTENT_FILTER));
                Toast.makeText(MainActivity.this, "Started", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected");
        Intent activityIntent = new Intent(this, ActivityRecognizedService.class);
        PendingIntent pendingActivityIntent = PendingIntent.getService(this, 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mClient, UPDATE_INTERVAL, pendingActivityIntent);
    }

    @Override
    protected void onDestroy() {
        mClient.disconnect();
        LocalBroadcastManager.getInstance(MainActivity.this).unregisterReceiver(mBroadcastReceiver);
        Toast.makeText(MainActivity.this, "Activity Recognition Stopped!", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed");
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra(Constants.MESSAGE_KEY);
            int confidenceValue = intent.getIntExtra(Constants.CONFIDENCE_KEY, 0);
            boolean isLightDark = intent.getBooleanExtra(Constants.LIGHT_KEY, false);
            //time
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.UK);
            Date date = new Date(System.currentTimeMillis());
            //just to update ui
            mActivityType.setText(message);
            mConfidenceLevel.setText("" + confidenceValue+" time "+formatter.format(date));
            mStatus.setText("Status: Receiving updates");
            Log.d(TAG, "onReceive message: " + message + " Confidence Value: " + confidenceValue + " is Light Dark: " + isLightDark);

        }
    };


}