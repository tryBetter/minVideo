package com.min.view;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.min.base.BaseActivity;
import com.min.bean.VideoBean;
import com.min.event.PortEvent;
import com.min.h3video.R;
import com.min.model.Model;
import com.min.mvp.IMVPView;
import com.min.presenter.VideoFilePresenter;
import com.min.utils.FileUtils;
import com.min.utils.RxUtil;
import com.min.utils.SerialPortType;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

public class VideoMainActivity extends BaseActivity implements IMVPView {
    public static String path = "/storage/emulated/0/Movies";
    public static String loopVideoPath = path+"/vendor.mp4";
    /*public static String path = "/data/Movies";
    public static String loopVideoPath = path+"/Lollipop.3GP";*/
    private static final int MSG_READ_FINISH = 1;
    private File rootFile;
    private VideoFilePresenter videoFilePresenter;
    /** 包含有视频文件夹集合 **/
    private List<VideoBean> videoBeans = new ArrayList<VideoBean>();
    private int playIndex = 0;
    private String TAG = "VideoListActivity";
    private VideoView videoView;

    // 操作成功返回的信息
    private byte[] oprateSuccess = new byte[]{(byte)0x8f,(byte)0x8f,(byte)0x22,(byte)0x08,
            (byte)0x00,(byte)0xff,(byte)0xff,(byte)0x2a};

    // 操作失败返回的信息
    private byte[] oprateFail = new byte[]{(byte)0x8f,(byte)0x8f,(byte)0x21,(byte)0x08,
            (byte)0x00,(byte)0xff,(byte)0xff,(byte)0x29};
    @Override
    protected int getLayoutId() {
        return R.layout.activity_player;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {

        videoView = (VideoView) findViewById(R.id.view_video);
        android.widget.MediaController mediaController = new android.widget.MediaController(this);
        videoView.setMediaController(mediaController);

        File loopVideo = new File(loopVideoPath);
        if(loopVideo.exists()){
            videoView.setVideoPath(loopVideoPath);
            videoView.start();
        }else{
            Toast.makeText(this, "vendor.mp4 没有找到！", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     *  初始化视频数据
     */
    @Override
    protected void initData() {
        videoFilePresenter = new VideoFilePresenter();
        videoFilePresenter.attachView(this);

        rootFile = new File(path);
//            ReadVideoFileByRxjava();
        if (rootFile.exists()) {
            videoFilePresenter.getVideoData(rootFile);
        } else {
            Toast.makeText(this, "Movies 目录不存在！", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void initToolbar(Bundle savedInstanceState) {

    }

    @Override
    protected void initListeners() {
        // 注册播放完成监听
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if(playIndex==0){
                    videoView.start();
                }
            }
        });
    }

    private void ReadVideoFileByRxjava() {
        Observable.just(rootFile)
            .flatMap(new Func1<File, Observable<File>>() {
                @Override
                public Observable<File> call(File file) {
                    return RxUtil.listFiles(file);
                }
            })
            .subscribe(
                new Subscriber<File>() {
                    @Override
                    public void onCompleted() {
                        Log.d("danxx", "onCompleted");
                        if (videoBeans.size() > 0) {
//                            mAdapter.setData(videoBeans);
//                            mAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(VideoMainActivity.this, "sorry,没有读取到视频文件!", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(File file) {
                        String name = file.getName();
                        String size = FileUtils.showFileSize(file.length());
                        String path = file.getPath();
                        videoBeans.add(new VideoBean(name, path, size));
                        Log.d("danxx", "name--->" + name);
                    }
                }
            );
    }

    /**
     * 获取数据成功后回调
     *
     * @param data
     */
    @Override
    public void getDataSuccess(List<? extends Model> data) {
        if (data.size() > 0) {
            videoBeans.addAll((Collection<? extends VideoBean>) data);
        }else{
            Toast.makeText(this,"Movies目录下没有可播放的视频文件！",Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 获取数据失败
     *
     * @param e
     */
    @Override
    public void getDataError(Throwable e) {

    }

    @Override
    public void showProgress() {

    }

    @Override
    public void hideProgress() {

    }

    // TODO  接收串口数据
    @Override
    protected void onDataReceived(final byte[] buffer, int size) {
        if(buffer.length<=0){
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                StringBuilder receiveData = new StringBuilder();

                for(int i = 0 ; i < buffer.length;i++){
                    receiveData.append(Integer.toHexString(buffer[i]&255));
                }
                // 以16进制的形式打印所有数据
                Log.e(TAG,"ReceiveData = "+receiveData.toString());

                //　获取第一位校验数据，通过 &255 运算得到有效数据，去除符号位
                int checkResult = buffer[0] & 255;
                //　通过循环异或所有数据
                for (int j = 1; j < buffer.length - 1; j++) {
                    checkResult = (buffer[j] & 255) ^ checkResult;
                }

                if(checkResult == (buffer[7] & 255)){
                    // 校验成功
                    PortEvent portEvent = new PortEvent();
                    portEvent.serialTag = buffer[2] & 255;
                    switch (buffer[2] & 255){
                        case 1://获取文件列表
                            portEvent.portType = SerialPortType.GET_FILE_LIST ;
                            break;
                        case 2://获取当前播放的文件
                            portEvent.portType = SerialPortType.GET_PLAYING_FILE ;
                            break;
                        case 3://开始播放   8f 8f 03 08 00 ff ff 0b
                            portEvent.portType = SerialPortType.PLAY ;
                            videoView.start();
                            sendResult(true);
                            break;
                        case 4://暂停播放   8f 8f 04 08 00 ff ff 0c
                            portEvent.portType = SerialPortType.PAUSE ;
                            videoView.pause();
                            sendResult(true);
                            break;
                        case 5://停止播放   8f 8f 05 08 00 ff ff 0d
                            portEvent.portType = SerialPortType.STOP ;
                            videoView.stopPlayback();
                            sendResult(true);
                            break;
                        case 6://播放上一个   8f 8f 06 08 00 ff ff 0e
                            if(playIndex > 0){
                                playIndex--;
                                portEvent.portType = SerialPortType.PREVIOUS ;
                                videoView.setVideoPath(videoBeans.get(playIndex).path);
                                videoView.start();
                                sendResult(true);
                            }else{
                                Toast.makeText(VideoMainActivity.this,"已经是第一个视频了",Toast.LENGTH_SHORT).show();
                                sendResult(false);
                            }
                            break;
                        case 7://播放下一个    8f 8f 07 08 00 ff ff 0f
                            if(playIndex < videoBeans.size()-1){
                                playIndex++;
                                portEvent.portType = SerialPortType.NEXT ;
                                videoView.setVideoPath(videoBeans.get(playIndex).path);
                                videoView.start();
                                sendResult(true);
                            }else{
                                Toast.makeText(VideoMainActivity.this,"已经是最后一个视频了",Toast.LENGTH_SHORT).show();
                                sendResult(false);
                            }
                            break;
                        case 8://播放指定视频
                            portEvent.portType = SerialPortType.SELECT ;
                            break;
                        case 9://返回当前视频
                            portEvent.portType = SerialPortType.RETURN_VIDEO ;
                            break;
                        case 10://返回文件列表
                            portEvent.portType = SerialPortType.RETURN_LIST ;
                            break;
                        default:
                            sendResult(false);
                            break;
                    }
//                    EventBus.getDefault().post(portEvent);
                }
                Log.e(TAG, "checkResult = " + checkResult);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoFilePresenter.detachView();
    }

    private void sendResult(boolean isSuccess){
        try {
            if(isSuccess){
                    mOutputStream.write(oprateSuccess);
            }else{
                    mOutputStream.write(oprateFail);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
