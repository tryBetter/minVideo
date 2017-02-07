package com.min.utils;

/**
 * Created by w on 2017/1/28.
 */

public enum SerialPortType {


    GET_FILE_LIST(1), // 获取文件列表
    GET_PLAYING_FILE(2),// 获取当前播放的文件
    PLAY(3),// 开始播放
    PAUSE(4),// 暂停播放
    STOP(5),// 停止播放
    PREVIOUS(6),// 播放上一个
    NEXT(7),// 播放下一个
    SELECT(8),// 播放指定视频
    RETURN_VIDEO(9),// 返回当前视频
    RETURN_LIST(10),// 返回文件列表
    OPRATE_FAILE(33),// 操作失败
    OPRATE_SUCCESS(34);// 正确操作

    SerialPortType(int value) {
        
    }
}
