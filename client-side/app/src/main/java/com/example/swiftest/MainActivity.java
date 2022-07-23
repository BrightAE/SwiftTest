package com.example.swiftest;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {
    BandwidthTest bandwidthTest = new BandwidthTest(this);
    boolean isTesting = false;

    //for test
    class myTestThread extends Thread {
//        ArrayList<Double> speedSample;
        boolean finish;
        int current_index;
        ArrayList<Double> speedSample;

        myTestThread(ArrayList<Double> speedSample) {
            this.finish = false;
            this.current_index = 0;
            this.speedSample = speedSample;
        }

        public void run() {
            while (!finish) {
                try {
                    sleep(50);
                    current_index ++;
                    StringBuilder sb = new StringBuilder();
                    for (int i = speedSample.size() - 1; i >= 0; i--) {
                        double num = speedSample.get(i);
                        sb.append(num);
                        sb.append(",");
                    }
                    String result = sb.toString();

                    Log.d(Integer.toString(current_index), result);
                    if(!isTesting) break;
                } catch (InterruptedException e) {
                    Log.d("test_thread", "bug");
                }

                //draw on windows
            }
        }

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        Logger.addLogAdapter(new AndroidLogAdapter());

        TextView bandwidth_text = findViewById(R.id.bandwidth);
        TextView duration_text = findViewById(R.id.duration);
        TextView traffic_text = findViewById(R.id.traffic);
//        TextView network_text = findViewById(R.id.network);
        Button button = findViewById(R.id.start);
        button.setOnClickListener(view -> {
            if (isTesting) {
                isTesting = false;
                button.setText(R.string.start);
                bandwidthTest.stop();
            } else {
                isTesting = true;
                button.setText(R.string.stop);
                bandwidth_text.setText(R.string.testing);
                duration_text.setText(R.string.testing);
                traffic_text.setText(R.string.testing);
                // network_text.setText(R.string.testing);
                new Thread(() -> {
                    String bandwidth = "0";
                    String duration = "0";
                    String traffic = "0";
                    try {
                        bandwidthTest.SpeedTest();
                        bandwidth = bandwidthTest.bandwidth_Mbps;
                        duration = bandwidthTest.duration_s;
                        traffic = bandwidthTest.traffic_MB;
                        // network = bandwidthTest.networkType;
                        bandwidthTest.stop();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    String finalBandwidth = bandwidth + "  Mbps";
                    String finalDuration = duration + "  s";
                    String finalTraffic = traffic + "  MB";
                    // String finalNetwork = network;
                    runOnUiThread(() -> {
                        isTesting = false;
                        button.setText(R.string.start);
                        bandwidth_text.setText(finalBandwidth);
                        duration_text.setText(finalDuration);
                        traffic_text.setText(finalTraffic);
                        // network_text.setText(finalNetwork);
                    });
                }).start();

                myTestThread mtt = new myTestThread(bandwidthTest.speedSample);
                mtt.start();
                //for test

            }
        });
    }
}