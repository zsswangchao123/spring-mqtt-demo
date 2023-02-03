package com.example.springmqttdemo.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springmqttdemo.model.Student;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author baomidou
 * @since 2023-02-01
 */
@Mapper
public interface StudentMapper extends BaseMapper<Student> {

}
