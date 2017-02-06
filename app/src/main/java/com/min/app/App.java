package com.min.app;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.min.model.CacheManager;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;

import android_serialport_api.SerialPort;
import android_serialport_api.SerialPortFinder;


/**
 * Created by Danxx on 2016/5/30.
 */
public class App extends Application {

    public SerialPortFinder mSerialPortFinder = new SerialPortFinder();
    private SerialPort mSerialPort = null;
//    private String portPath = "/dev/ttyS1";
    private String portPath = "/dev/ttyS3";
    private int baudrate = 115200;
    private Context mContext;
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        //初始化缓存模块
        CacheManager.getInstance().init(mContext);

    }

    public Context getAppContext(){
        return mContext;
    }


    public SerialPort getSerialPort() throws SecurityException, IOException, InvalidParameterException {
        if (mSerialPort == null) {
           /* SharedPreferences sp = getSharedPreferences("com.min.serialport_preferences", MODE_PRIVATE);
            String path = sp.getString("DEVICE", "");
            int baudrate = Integer.decode(sp.getString("BAUDRATE", "-1"));

            if ( (path.length() == 0) || (baudrate == -1)) {
                throw new InvalidParameterException();
            }

            mSerialPort = new SerialPort(new File(path), baudrate, 0);*/
            mSerialPort = new SerialPort(new File(portPath), baudrate, 0);
        }
        return mSerialPort;
    }

    public void closeSerialPort() {
        if (mSerialPort != null) {
            mSerialPort.close();
            mSerialPort = null;
        }
    }

}
