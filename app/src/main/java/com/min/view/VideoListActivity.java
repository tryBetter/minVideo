package com.min.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


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
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

public class VideoListActivity extends BaseActivity implements IMVPView {
    private String path;
    private static final int MSG_READ_FINISH = 1;
    private VideoListAdapter mAdapter;
    private File rootFile;
    private VideoFilePresenter videoFilePresenter;
    private ListView videoListView;
    /**
     * 包含有视频文件夹集合
     **/
    private List<VideoBean> videoBeans = new ArrayList<VideoBean>();
    private int clickIndex ;
    private String TAG = "VideoListActivity";

    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override
    protected int getLayoutId() {
        return R.layout.activity_video_list;
    }

    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override
    protected void initViews(Bundle savedInstanceState) {
        Intent intent = getIntent();
        path = intent.getStringExtra("path");
        videoListView = (ListView) findViewById(R.id.videoListView);
        mAdapter = new VideoListAdapter(this);
        videoListView.setAdapter(mAdapter);
    }

    /**
     * Initialize the Activity data
     */
    @Override
    protected void initData() {
        videoFilePresenter = new VideoFilePresenter();
        videoFilePresenter.attachView(this);
        if (path != null && !TextUtils.isEmpty(path)) {
            rootFile = new File(path);
//            ReadVideoFileByRxjava();
            videoFilePresenter.getVideoData(rootFile);
        }
    }

    /**
     * Initialize the toolbar in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override
    protected void initToolbar(Bundle savedInstanceState) {

    }

    /**
     * Initialize the View of the listener
     */
    @Override
    protected void initListeners() {
        videoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                clickIndex = position ;
                VideoActivity.intentTo(VideoListActivity.this, ((VideoBean)mAdapter.getItem(position)).path, ((VideoBean)mAdapter.getItem(position)).name);
            }
        });
    }

    /**
     * 参考:http://blog.csdn.net/job_hesc/article/details/46242117
     */
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
                            Toast.makeText(VideoListActivity.this, "sorry,没有读取到视频文件!", Toast.LENGTH_LONG).show();
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
        mAdapter.setData((List<VideoBean>) data);
        mAdapter.notifyDataSetChanged();
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
                            break;
                        case 4://暂停播放   8f 8f 04 08 00 ff ff 0c
                            portEvent.portType = SerialPortType.PAUSE ;
                            break;
                        case 5://停止播放   8f 8f 05 08 00 ff ff 0d
                            portEvent.portType = SerialPortType.STOP ;
                            break;
                        case 6://播放上一个   8f 8f 06 08 00 ff ff 0e
                            if(clickIndex>0){
                                clickIndex--;
                                portEvent.path = ((VideoBean)mAdapter.getItem(clickIndex)).path;
                                portEvent.portType = SerialPortType.PREVIOUS ;
                            }else{
                                Toast.makeText(VideoListActivity.this,"已经是第一个视频了",Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case 7://播放下一个    8f 8f 07 08 00 ff ff 0f
                            if(clickIndex < mAdapter.getCount()-1){
                                clickIndex++;
                                portEvent.path = ((VideoBean)mAdapter.getItem(clickIndex)).path;
                                portEvent.portType = SerialPortType.NEXT ;
                            }else{
                                Toast.makeText(VideoListActivity.this,"已经是最后一个视频了",Toast.LENGTH_SHORT).show();
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
                        case 33://操作失败
                            portEvent.portType = SerialPortType.OPRATE_FAILE ;
                            break;
                        case 34://正确操作
                            portEvent.portType = SerialPortType.OPRATE_SUCCESS ;
                            break;
                    }
                    EventBus.getDefault().post(portEvent);
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

    class VideoListAdapter extends BaseAdapter {

        private Context context;
        private LayoutInflater inflate;
        private List<VideoBean> dataList;

        public VideoListAdapter(Context context) {
            this.context = context;
            inflate = LayoutInflater.from(context);
        }
        
        public void setData(List<VideoBean> dataList){
            this.dataList = dataList;
        }

        @Override
        public int getCount() {
            if(dataList!=null){
                return dataList.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return dataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MyViewHolder viewHolder;
            if(convertView == null){
                convertView = inflate.inflate(R.layout.item_videos_layout,parent,false);
                viewHolder = new MyViewHolder(convertView);
                convertView.setTag(viewHolder);
            }else{
                viewHolder = (MyViewHolder) convertView.getTag();
            }
            viewHolder.tvName.setText(dataList.get(position).name);
            viewHolder.tvSize.setText(dataList.get(position).size);
            return convertView;
        }

        class MyViewHolder {
            View mView;
            ImageView ivPic;
            TextView tvName, tvSize, tvlength;

            public MyViewHolder(View itemView) {
                super();
                ivPic = (ImageView) itemView.findViewById(R.id.ivPic);
                tvName = (TextView) itemView.findViewById(R.id.tvName);
                tvSize = (TextView) itemView.findViewById(R.id.tvSize);
                tvlength = (TextView) itemView.findViewById(R.id.tvlength);
                mView = itemView;
            }

            protected View getView() {
                return mView;
            }
        }
    }
}
