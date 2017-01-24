package com.min.view;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.VideoView;

import com.min.h3video.R;


public class PlayerActivity extends Activity {

    private VideoView videoView;
    
    private String[] videos = {"big_buck_1080p.MP4","sdl_1080p.mp4","视频压缩编码动画-1.flv","视频压缩编码动画-2.flv"};
    
    private int index = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        initView();
    }

    private void initView() {
        videoView = (VideoView) findViewById(R.id.view_video);
        android.widget.MediaController mediaController = new android.widget.MediaController(this);
        videoView.setMediaController(mediaController);
        
    }

    private void startVideo(String videoFilePath) {
        /*if(videoView.isPlaying()){
            videoView
        }*/
        videoView.setVideoPath(videoFilePath);
//        videoView.setVideoPath("/storage/emulated/0/minVideo/big_buck_1080p.MP4");
        videoView.start();
        videoView.requestFocus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startVideo("/storage/emulated/0/minVideo/big_buck_1080p.MP4");
        
    }
}
