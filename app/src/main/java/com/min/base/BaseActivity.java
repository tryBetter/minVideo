package com.min.base;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;


/**
 * Created by Danxx on 2016/6/15.
 * 基本Activity
 */
public abstract class BaseActivity extends SerialPortActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(this.getLayoutId());
        this.initToolbar(savedInstanceState);
        this.initViews(savedInstanceState);
        this.initData();
        this.initListeners();
    }

    /**
     * Fill in layout id
     *
     * @return layout id
     */
    protected abstract int getLayoutId();


    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    protected abstract void initViews(Bundle savedInstanceState);
    /**
     * Initialize the Activity data
     */
    protected abstract void initData();
    /**
     * Initialize the toolbar in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    protected abstract void initToolbar(Bundle savedInstanceState);

    /**
     * Initialize the View of the listener
     */
    protected abstract void initListeners();


    /*********
     * Toast *
     *********/

    public void showToast(String msg) {
        if(msg != null){
            Toast.makeText(this,msg , Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    /**
     * 设置标题
     * @param strTitle  标题
     * @param showHome  显示返回按钮
     */
    public void setTitle(String strTitle, boolean showHome){
        setTitle(strTitle);
    }
}
