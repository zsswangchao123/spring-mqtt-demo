package com.example.springmqttdemo.model;


import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fhs.core.trans.vo.TransPojo;
import lombok.Data;

@Data
@TableName("school")
public class School implements TransPojo {


    @TableId
    private String id;

    private String schoolName;


    private String  language;
}
