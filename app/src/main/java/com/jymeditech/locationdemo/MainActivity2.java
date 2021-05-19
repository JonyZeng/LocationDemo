package com.jymeditech.locationdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttToken;

public class MainActivity2 extends AppCompatActivity {
    private static final String TAG = "MainActivity2";
    private Intent mIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        MyMqttService.getInstance().setMqttConnectListener(new MyMqttService.MqttConnectListener() {
            @Override
            public void onSuccess(IMqttToken token) {
                Log.i(TAG, "onSuccess: ");
                Toast.makeText(getApplicationContext(), "You are connected to SDAIA", Toast.LENGTH_SHORT).show();
//                disConnectImg.setVisibility(View.GONE);
            }

            @Override
            public void onFailure() {
//                disConnectImg.setVisibility(View.VISIBLE);
                Toast.makeText(getApplicationContext(), "You are disconnected to SDAIA", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "onFailure: ");
            }
        });
        mIntent = new Intent(MainActivity2.this, MyMqttService.class);
        //开启服务
        startService(mIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mIntent == null) return;
        stopService(mIntent);
    }

}