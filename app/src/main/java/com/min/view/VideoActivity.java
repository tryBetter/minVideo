/*
 * Copyright (C) 2015 Zhang Rui <bbcallen@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.min.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.VideoView;

import com.min.event.PortEvent;
import com.min.h3video.R;
import com.min.model.RecentMediaStorage;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class VideoActivity extends Activity {
    private static final String TAG = "VideoActivity";

    private String mVideoPath;

    private VideoView videoView;

    public static Intent newIntent(Context context, String videoPath, String videoTitle) {
        Intent intent = new Intent(context, VideoActivity.class);
        intent.putExtra("videoPath", videoPath);
        intent.putExtra("videoTitle", videoTitle);
        return intent;
    }

    public static void intentTo(Context context, String videoPath, String videoTitle) {
        context.startActivity(newIntent(context, videoPath, videoTitle));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        mVideoPath = getIntent().getStringExtra("videoPath");

        if (!TextUtils.isEmpty(mVideoPath)) {
            new RecentMediaStorage(this).saveUrlAsync(mVideoPath);
        }

        videoView = (VideoView) findViewById(R.id.view_video);

        android.widget.MediaController mediaController = new android.widget.MediaController(this);
        videoView.setMediaController(mediaController);
        videoView.setVideoPath(mVideoPath);
        videoView.start();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void controlVideo(PortEvent event) {
        switch (event.serialTag){
            case 1:
                break;
            case 2:
                break;
            case 3:
                videoView.start();
                break;
            case 4:
                videoView.pause();
                break;
            case 5:
                videoView.stopPlayback();
                break;
            case 6:
            case 7:
                if (event.path != null) {
                    videoView.setVideoPath(event.path);
                    videoView.start();
                }
                break;
            case 8:
                break;
            case 9:
                break;
            case 10:
                break;
            case 33:
                break;
            case 34:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoView.resume();
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        videoView.stopPlayback();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
