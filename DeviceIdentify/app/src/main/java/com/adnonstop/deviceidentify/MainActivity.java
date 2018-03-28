package com.adnonstop.deviceidentify;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnDeviceIdentify = (Button) findViewById(R.id.btn_obtain_device_identify);
        btnDeviceIdentify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


//                String uniquePsuedoDeviceID = DeviceUtils.getUniquePsuedoDeviceID();
//
//                Log.i(TAG, "onClick: uniquePsuedoDeviceID = " + uniquePsuedoDeviceID);
//
////                String uniqueID = UUID.randomUUID().toString();
////                Log.i(TAG, "onClick: uniqueID = "+uniqueID);
//
//
//                String myUUID = DeviceUtils.getMyUUID(MainActivity.this);
//                Log.i(TAG, "onClick: myUUID = "+myUUID);


//                boolean isRealPhone = DeviceUtils.checkIsRealPhone();
//                Log.i(TAG, "onCreate: isRealPhone = " + isRealPhone);

//                Boolean notHasLightSensorManager = DeviceUtils.notHasLightSensorManager(MainActivity.this);
//                Log.i(TAG, "onClick: notHasLightSensorManager = " + notHasLightSensorManager);

                if (DeviceUtils.checkIsNotRealPhoneAccordingCpu()
                        || DeviceUtils.notHasLightSensorManager(MainActivity.this)) {
                    Log.i(TAG, "onClick: 虚拟机啦");
                } else {

                    Log.i(TAG, "onClick: 真机啦");
                }
            }
        });


    }


}
