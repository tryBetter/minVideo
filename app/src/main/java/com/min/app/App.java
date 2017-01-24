package com.min.app;

import android.app.Application;
import android.content.Context;

import com.min.model.CacheManager;


/**
 * Created by Danxx on 2016/5/30.
 */
public class App extends Application {
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

}
