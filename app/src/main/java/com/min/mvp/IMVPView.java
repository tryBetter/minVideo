package com.min.mvp;


import com.min.model.Model;

import java.util.List;

/**
 * Created by Danxx on 2016/6/17.
 * 接口
 */
public interface IMVPView extends MvpView {

    /**
     * 获取数据成功后回调
     * @param data
     */
    void getDataSuccess(List<? extends Model> data);

    /**
     * 获取数据失败
     * @param e
     */
    void getDataError(Throwable e);

    void showProgress();

    void hideProgress();
}
