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
import com.min.h3video.R;
import com.min.model.Model;
import com.min.mvp.IMVPView;
import com.min.presenter.VideoFilePresenter;
import com.min.utils.FileUtils;
import com.min.utils.RxUtil;

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
            ((MyViewHolder) viewHolder).tvName.setText(dataList.get(position).name);
            ((MyViewHolder) viewHolder).tvSize.setText(dataList.get(position).size);
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
