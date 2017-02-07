package com.min.view;

import android.media.MediaPlayer;
import android.opengl.GLU;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

public class VideoMainActivity extends BaseActivity implements IMVPView {
    /*public static String ROOT_DIR_PATH = "/storage/emulated/0/Movies";
    public static String LOOP_VIDEO_PATH = ROOT_DIR_PATH +"/vendor.mp4";*/
    public static String ROOT_DIR_PATH = "/data/Movies";
    public static String LOOP_VIDEO_PATH = ROOT_DIR_PATH+"/Lollipop.3GP";
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
    
    private SparseIntArray videoIndexArray = new SparseIntArray();

    // 所有视频文件的字符串数组
    String[] videoNameList ;
    
    @Override
    protected int getLayoutId() {
        return R.layout.activity_player;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {

        videoView = (VideoView) findViewById(R.id.view_video);
        android.widget.MediaController mediaController = new android.widget.MediaController(this);
        videoView.setMediaController(mediaController);

        File loopVideo = new File(LOOP_VIDEO_PATH);
        if(loopVideo.exists()){
            videoView.setVideoPath(LOOP_VIDEO_PATH);
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

        rootFile = new File(ROOT_DIR_PATH);
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
            videoNameList = new String[videoBeans.size()];
            for(int i = 0 ; i < videoBeans.size() ; i++){
                videoNameList[i] = videoBeans.get(i).name;
            }
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
        if (buffer.length <= 0) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                StringBuilder receiveData = new StringBuilder();

                for (int i = 0; i < buffer.length; i++) {
                    receiveData.append(Integer.toHexString(buffer[i] & 0xff)).append(" ");
                }
                // 以16进制的形式打印所有数据
                Log.e(TAG,"ReceiveData = "+receiveData.toString());

                //　获取第一位校验数据，通过 &255 运算得到有效数据，去除符号位
                int checkResult = buffer[0] & 0xff;
                //　通过循环异或所有数据
                for (int j = 1; j < buffer.length - 1; j++) {
                    checkResult = (buffer[j] & 0xff) ^ (checkResult & 0xff);
                }

                if(checkResult == (buffer[buffer.length - 1] & 0xff)){
                    // 校验成功
                    switch (buffer[2] & 0xff){
                        case 1://获取文件列表
//                            portEvent.portType = SerialPortType.GET_FILE_LIST ;
                            break;
                        case 2://获取当前播放的文件
//                            portEvent.portType = SerialPortType.GET_PLAYING_FILE ;
                            break;
                        case 3://开始播放   8f 8f 03 08 00 ff ff 0b
//                            portEvent.portType = SerialPortType.PLAY ;
                            videoView.start();
                            sendResult(true);
                            break;
                        case 4://暂停播放   8f 8f 04 08 00 ff ff 0c
//                            portEvent.portType = SerialPortType.PAUSE ;
                            videoView.pause();
                            sendResult(true);
                            break;
                        case 5://停止播放   8f 8f 05 08 00 ff ff 0d
//                            portEvent.portType = SerialPortType.STOP ;
                            videoView.stopPlayback();
                            sendResult(true);
                            break;
                        case 6://播放上一个   8f 8f 06 08 00 ff ff 0e
                            if(playIndex > 0){
                                playIndex--;
//                                portEvent.portType = SerialPortType.PREVIOUS ;
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
//                                portEvent.portType = SerialPortType.NEXT ;
                                videoView.setVideoPath(videoBeans.get(playIndex).path);
                                videoView.start();
                                sendResult(true);
                            }else{
                                Toast.makeText(VideoMainActivity.this,"已经是最后一个视频了",Toast.LENGTH_SHORT).show();
                                sendResult(false);
                            }
                            break;          //     -113, -113, 10, 11, 0, 97, 46, 51, 71, 80, 106
                        case 8://播放指定视频(a.3GP)  8f 8f 08 0b 00 61 2e 33 47 50 6a
//                            portEvent.portType = SerialPortType.SELECT;
                            File selectedVideo = new File(ROOT_DIR_PATH+"/"+new String(Arrays.copyOfRange(buffer,5,buffer.length-1)));
                            if(selectedVideo.exists()){
                                videoView.setVideoPath(selectedVideo.getPath());
                            }else{
                                Toast.makeText(VideoMainActivity.this,"视频不存在！",Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case 9://返回当前视频
//                            portEvent.portType = SerialPortType.RETURN_VIDEO ;
                            break;
                        case 10://返回文件列表  8f 8f 0a 08 00 ff ff 02
//                            portEvent.portType = SerialPortType.RETURN_LIST ;
                            sendFileList();
                            break;
                        default:
                            sendResult(false);
                            break;
                    }
//                    EventBus.getDefault().post(portEvent);
                }else{
                    sendResult(false);
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
    
    private void sendFileList(){
        try {
            if (rootFile.exists() && rootFile.isDirectory() && videoNameList != null && videoNameList.length > 0) {
                int fileCount = videoNameList.length;
                for(int i = 0 ; i < fileCount ; i++){// 视频列表名称循环
                    // 视频名称的Byte长度
                    int videoNameByteCount = videoNameList[i].getBytes().length;
                    byte[] videoLists = new byte[6+videoNameByteCount];
                    videoLists[0] = (byte) (0x8f);// 两位信息头 0x8f
                    videoLists[1] = (byte) (0x8f);
                    videoLists[2] = (byte) (0x0a);// 报文标识
                    videoLists[3] = (byte) (6 + videoNameByteCount);// 数据包长度，不包含校验位
                    videoLists[4] = (byte) (0x00);
                    
                    for(int j = 0 ; j < videoNameByteCount ; j++){// 单个文件名称的转码与存储
                        videoLists[5 + j] = (byte) (videoNameList[i].getBytes()[j] & 0xff);
                    }

                    // 第一位数据通过 &255 去除符号位
                    byte checkResult = (byte) (videoLists[0] & 0xff);
                    // 循环从第二位开始进行异或运算到倒数第二位
                    for(int k = 1 ; k < 5+videoNameByteCount ; k++){
                        checkResult = (byte) ((videoLists[k] & 0xff) ^ (checkResult & 0xff));
                    }
                    // 存储最后的校验位
                    videoLists[5 + videoNameByteCount] = (byte) (checkResult & 0xff);
//                    Log.e(TAG,Arrays.toString(videoLists));
                    mOutputStream.write(videoLists);
                    sendResult(true);
                }
            }else{
                sendResult(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResult(false);
        }
    }

    /*if(msgId == GET_FILE_lIST)           // 获取视频目录下的所有视频文件
    {
        qDebug()<<"cmd = 0x01";

        for(int i = 0; i < VedioToPlayList.size(); i++)
        {
            QString file_name;
            cherk_or = 0;
            memset(wr_data, 0, sizeof(wr_data));
//            file_name = VedioToPlayList.at(i).section('/', 3, 3);
            file_name = VedioToPlayList.at(i).section('/', 4, 4);
            file_length = file_name.toLocal8Bit().length();
            wr_length = file_length + 6;
            memset(wr_data, 0, sizeof(wr_data));
            wr_data[0] = 0x8f;
            wr_data[1] = 0x8f;
            wr_data[2] = PUT_FILE_LIST;
            wr_data[3] =  wr_length;
            wr_data[4] = 0;

            strcpy(&wr_data[5], file_name.toLocal8Bit().constData());

            for(int i =0 ; i < wr_length - 1 ;i ++)
            {
                cherk_or =  cherk_or^wr_data[i];
            }

            wr_data[wr_length - 1] = cherk_or;   //填写校验位
            serial.write(wr_data, wr_length);
        }
    }
    else if(msgId == GET_CURRENT_FILE)      //获取当前播放的文件
    {
        qDebug()<<"cmd = 0x02";
        cherk_or = 0;
        QString file_name;
//        file_name = current_file.section('/', 3, 3);
        file_name = current_file.section('/', 4, 4);
        file_length = file_name.toLocal8Bit().length();
        memset(wr_data, 0, sizeof(wr_data));
        wr_length = file_length + 6;
        wr_data[0] = 0x8f;
        wr_data[1] = 0x8f;
        wr_data[2] = PUT_CURRENT_FILE;      //ID
        wr_data[3] =  wr_length;
        wr_data[4] = 0;

        strcpy(&wr_data[5], file_name.toLocal8Bit().constData());


        for(int i =0 ; i < wr_length - 1 ;i ++)
        {
            cherk_or =  cherk_or^wr_data[i];
        }

        wr_data[wr_length - 1] = cherk_or;   //填写校验位
        serial.write(wr_data, wr_length);
    }
    else if(msgId == START_PLAY)      //开始播放
    {
        qDebug()<<"cmd = 0x03";
        if (!vlcPlayer)
            return;

        if (libvlc_media_player_is_playing(vlcPlayer))
        {
        }
        else
        {
            *//* Play again *//*
            libvlc_media_player_play(vlcPlayer);
            playBut->setText("Pause");
        }
    }
    else if(msgId == PAUSE_PLAY)      //暂停播放
    {
        qDebug()<<"cmd = 0x04";
        if (!vlcPlayer)
            return;

        if (libvlc_media_player_is_playing(vlcPlayer))
        {
            *//* Pause *//*
            libvlc_media_player_pause(vlcPlayer);
            playBut->setText("Play");
        }
    }
    else if(msgId == STOP_PLAY)      //停止播放
    {
        qDebug()<<"cmd = 0x05";
        this->stop();
    }
    else if(msgId == PLAY_AFTER)      //播放下一文件
    {
        qDebug()<<"cmd = 0x06";
        this->stop();
        if(ListId >= (TotalVedio-1) )
        {
            ListId = 0;
        }else
            ListId = ListId + 1;
        this->playNum(ListId);
        current_file = VedioToPlayList.at(ListId);
    }
    else if(msgId == PLAY_BEFORE)      //播放上一文件
    {
        qDebug()<<"cmd = 0x07";
        this->stop();
        if(ListId == 0 )
        {
            ListId = TotalVedio-1;
        }else
            ListId = ListId - 1;
        this->playNum(ListId);
        current_file = VedioToPlayList.at(ListId);
    }
    else if(msgId == PLAY_TARGET_FILE)      //播放指定的文件
    {
        qDebug()<<"cmd = 0x08";
        rd_length = *(unsigned short *)&fullMsgBuf[3];
        file_length = rd_length - 6;
        fullMsgBuf[rd_length - 1] = '\0';
        QString target_file = QString::fromLocal8Bit(&fullMsgBuf[5]);
        QString target_play_flie;     //全路径
        qDebug()<<"play file:"<<target_file;

        //检查该文件是否在播放列表里面
        if(VedioNameList.contains(target_file))
        {
            //播放文件
            this->stop();
            target_play_flie.append(CurrentVideoDirStr);
            target_play_flie.append(target_file);
            playFile(target_play_flie);
        }
        else
        {
            //发送故障代码
            sendErrorCode(ERROR_NOFILE);
        }
    }
}*/
    
}
