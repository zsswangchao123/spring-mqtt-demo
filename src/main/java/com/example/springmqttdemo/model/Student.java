package com.example.springmqttdemo.model;

import cn.hutool.core.util.HexUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fhs.core.trans.anno.Trans;
import com.fhs.core.trans.constant.TransType;
import com.fhs.core.trans.vo.TransPojo;
import lombok.Data;

@Data
@TableName("student")
public class Student implements TransPojo {

	@TableId
	private String id;

	// 字典翻译 ref为非必填
	@Trans(type = TransType.DICTIONARY, key = "sex", ref = "sexName")
	private Integer sex;

	// 这个字段可以不写，实现了TransPojo接口后有一个getTransMap方法，sexName可以让前端去transMap取
	@TableField(exist = false)
	private String sexName;

	// SIMPLE 翻译，用于关联其他的表进行翻译 schoolName 为 School 的一个字段
	@Trans(type = TransType.AUTO_TRANS, key = "school")
	private String schoolId;

}
