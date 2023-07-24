package com.example.springmqttdemo.model;

import lombok.Data;

/**
 * 身份证消息
 */
@Data
public class IdcardInfo {

    // 姓名
    private String name;

    // 性别
    private String sex;

    // 民族
    private String nation;

    // 出生日期
    private String birth;

    // 地址
    private String address;

    // 身份证号
    private String idNumber;

    // 签发机关
    private String issue;

    // 有效期限
    private String validPeriod;

}
