package com.adnonstop.deviceidentify;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;

import static android.content.Context.SENSOR_SERVICE;

/**
 * Created by admin on 2017/8/10.
 * 获取设备id
 */

public class DeviceUtils {
    private static final String TAG = "DeviceUtils";
    public static final int REQUESTCODE_PERMISSION_READ_PHONE_STATE = 0x0001;
    private static final int REQUESTCODE_PERMISSION_BLUETOOTH = 0x0002;


//    public static String getAndroidID(Context context) {
//        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
//    }
//
//    public static String getPhoneInfo(Context context) {
//        TelephonyManager tm = (TelephonyManager) context
//                .getSystemService(Context.TELEPHONY_SERVICE);
//        return tm.getDeviceId();
//    }


    /**
     * Return pseudo unique ID
     *
     * @return ID
     */
    public static String getUniquePsuedoDeviceID() {
        // If all else fails, if the user does have lower than API 9 (lower
        // than Gingerbread), has reset their device or 'Secure.ANDROID_ID'
        // returns 'null', then simply the ID returned will be solely based
        // off their Android device information. This is where the collisions
        // can happen.
        // Thanks http://www.pocketmagic.net/?p=1662!
        // Try not to use DISPLAY, HOST or ID - these items could change.
        // If there are collisions, there will be overlapping data
        String m_szDevIDShort = "35" +
                (Build.BOARD.length() % 10) +
                (Build.BRAND.length() % 10) +
                (Build.CPU_ABI.length() % 10) +
                (Build.DEVICE.length() % 10) +
                (Build.MANUFACTURER.length() % 10) +
                (Build.MODEL.length() % 10) +
                (Build.PRODUCT.length() % 10);
//        Log.i("", "getUniquePsuedoDeviceID: "+m_szDevIDShort);
        // Thanks to @Roman SL!
        // http://stackoverflow.com/a/4789483/950427
        // Only devices with API >= 9 have android.os.Build.SERIAL
        // http://developer.android.com/reference/android/os/Build.html#SERIAL
        // If a user upgrades software or roots their device, there will be a duplicate entry
        String serial = null;
        try {
            serial = Build.class.getField("SERIAL").get(null).toString();

            // Go ahead and return the serial for api => 9
            return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
        } catch (Exception exception) {
            // String needs to be initialized
            serial = "serial"; // some value
        }

        // Thanks @Joe!
        // http://stackoverflow.com/a/2853253/950427
        // Finally, combine the values we have found by using the UUID class to create a unique identifier
        return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
    }


    public static String getMyUUID(AppCompatActivity context) {
        String uniqueId = null;
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUESTCODE_PERMISSION_READ_PHONE_STATE);
        } else {

            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            String tmDevice, tmSerial, tmPhone, androidId;

            tmDevice = "" + tm.getDeviceId();

            tmSerial = "" + tm.getSimSerialNumber();

            androidId = "" + android.provider.Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

            UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());

            uniqueId = deviceUuid.toString();

//            Log.i("debug", "uuid=" + uniqueId);

            return uniqueId;

        }

        return uniqueId;
    }


    /**
     * 判断是否存在光传感器来判断是否为模拟器
     * 部分真机也不存在温度和压力传感器。其余传感器模拟器也存在。
     *
     * @return true 为模拟器
     */
    public static Boolean notHasLightSensorManager(Context context) {
        SensorManager sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        Sensor sensor8 = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT); //光
        if (null == sensor8) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断蓝牙是否有效来判断是否为模拟器
     *
     * @return true 为模拟器
     */
    public static boolean notHasBlueTooth(AppCompatActivity context) {

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {


            ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.BLUETOOTH}, REQUESTCODE_PERMISSION_BLUETOOTH);
        } else {

            BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
            if (ba == null) {
                return true;
            } else {
                // 如果有蓝牙不一定是有效的。获取蓝牙名称，若为null 则默认为模拟器
                String name = ba.getName();
                if (TextUtils.isEmpty(name)) {
                    return true;
                } else {
                    return false;
                }
            }
        }

        return false;
    }

    /**
     * 根据部分特征参数设备信息来判断是否为模拟器
     *
     * @return true 为模拟器
     */
    public static boolean isFeatures() {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.toLowerCase().contains("vbox")
                || Build.FINGERPRINT.toLowerCase().contains("test-keys")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT);
    }

    private static String readCpuInfo() {
        String result = "";
        try {
            String[] args = {"/system/bin/cat", "/proc/cpuinfo"};
            ProcessBuilder cmd = new ProcessBuilder(args);

            Process process = cmd.start();
            StringBuffer sb = new StringBuffer();
            String readLine = "";
            BufferedReader responseReader = new BufferedReader(new InputStreamReader(process.getInputStream(), "utf-8"));
            while ((readLine = responseReader.readLine()) != null) {
                sb.append(readLine);
            }
            responseReader.close();
            result = sb.toString().toLowerCase();
        } catch (IOException ex) {
        }
        return result;
    }

    /**
     * 判断cpu是否为电脑来判断 模拟器
     *
     * @return true 为模拟器
     */
    public static boolean checkIsNotRealPhoneAccordingCpu() {
        String cpuInfo = readCpuInfo();
        if ((cpuInfo.contains("intel") || cpuInfo.contains("amd"))) {
            return true;
        }
        return false;
    }

}
