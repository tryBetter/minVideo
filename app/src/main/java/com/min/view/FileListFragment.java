package com.min.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.min.base.BaseFragment;
import com.min.bean.FileBean;
import com.min.h3video.R;
import com.min.model.Model;
import com.min.mvp.IMVPView;
import com.min.presenter.VideoFilePresenter;

import java.util.List;

import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;

/**
 *视频列表页
 */
public class FileListFragment extends BaseFragment implements IMVPView {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private FileListAdapter mAdapter;
    private View rootView;
    private ListView filesListView;
    private boolean isRefreshing;
    private int mScrollThreshold = 4;
    private VideoFilePresenter videoFilePresenter;
    private PtrFrameLayout refreshLayout;
    /**
     */
    public static FileListFragment newInstance(String param1, String param2) {
        FileListFragment fragment = new FileListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    protected View getContentView(LayoutInflater inflater, ViewGroup container) {
        rootView = inflater.inflate(R.layout.fragment_list, container, false);
        return rootView;
    }

    @Override
    protected void initViews(View contentView) {
        filesListView = (ListView) rootView.findViewById(R.id.filesListview);

        mAdapter = new FileListAdapter(getActivity());
        
        filesListView.setAdapter(mAdapter);

        refreshLayout = (PtrFrameLayout) rootView.findViewById(R.id.refreshLayout);
        // 这句话是为了，第一次进入页面的时候显示加载进度条
        refreshLayout.autoRefresh(true,1000);
    }

    @Override
    protected void initListeners() {
        //item点击监听
        filesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), VideoListActivity.class);
                intent.putExtra("ROOT_DIR_PATH", ((FileBean)mAdapter.getItem(position)).path);
                getActivity().startActivity(intent);
            }

        });
        // 下拉刷新监听
        refreshLayout.setPtrHandler(new PtrHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                refresh();
               /* frame.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshLayout.refreshComplete();
                    }
                }, 1800);*/
            }

            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header);
            }
        });
    }

    @Override
    protected void initDatas() {
        videoFilePresenter = new VideoFilePresenter();
        videoFilePresenter.attachView(this);
        videoFilePresenter.getFileData(false);
    }


    /**
     * 重新扫描,刷新文件
     */
    public void refresh(){
        if(!isRefreshing){
            videoFilePresenter.getFileData(true);
        }
    }

    @Override
    public void getDataSuccess(List<? extends Model> data) {
        Log.d("danxx" ,"getDataSuccess--->"+data.size());
        mAdapter.setData((List<FileBean>) data);
        mAdapter.notifyDataSetChanged();
//        showToast("读取到了"+data.size()+"个目录");
    }

    @Override
    public void getDataError(Throwable e) {
        e.printStackTrace();
        showToast("视频文件读取失败，请稍后重试！");
    }

    @Override
    public void showProgress() {
        isRefreshing  = true;
        refreshLayout.autoRefresh(true);
    }

    @Override
    public void hideProgress() {
        isRefreshing = false;
        refreshLayout.refreshComplete();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        videoFilePresenter.detachView();
    }

    class FileListAdapter extends BaseAdapter {

        private List<FileBean> fileBeanList;
        private Context context;
        private LayoutInflater inflater;
        public FileListAdapter(Context context) {
            this.context = context;
            inflater = LayoutInflater.from(context);
        }

        public void setData(List<FileBean> fileBeanList) {
            if (this.fileBeanList != null && this.fileBeanList.size() > 0) {
                this.fileBeanList.clear();
            }
            this.fileBeanList = fileBeanList;
        }

        @Override
        public int getCount() {
            if (fileBeanList != null) {
                return fileBeanList.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return fileBeanList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MyViewHolder viewHolder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_files_layout, parent, false);
                viewHolder = new MyViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (MyViewHolder) convertView.getTag();
            }
            viewHolder.tvPath.setText(fileBeanList.get(position).name);
            viewHolder.tvCount.setText(fileBeanList.get(position).count + "个视频文件");
            return convertView;
        }

        class MyViewHolder {
            View mView;
            TextView tvPath,tvCount;
            public MyViewHolder(View itemView) {
                super();
                mView = itemView;
                tvPath = (TextView) itemView.findViewById(R.id.tvPath);
                tvCount = (TextView) itemView.findViewById(R.id.tvCount);
            }
        }
    }
}
