package com.example.springmqttdemo.tio;

/**
 * 通用系统常量
 */
public interface SysCommonConstant {

    //强制取消批量控制时间30分钟
    int FORCE_CANCEL_TIME = 1000 * 60 * 30;

    //可用处理器数
    int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();
}
