package com.example.springmqttdemo.handle;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.springmqttdemo.mapper.SchoolMapper;
import com.example.springmqttdemo.model.School;
import com.example.springmqttdemo.model.Student;
import com.fhs.core.trans.anno.AutoTrans;
import com.fhs.trans.service.AutoTransable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@AutoTrans(namespace = "school", globalCache = true, fields = { "schoolName" })
public class ConfigI18nAutoTrans implements AutoTransable<School> {

	private final SchoolMapper schoolMapper;

	private final HttpServletRequest request;

	@Override
	public List<School> selectByIds(List<?> ids) {

		String language = RequestContextUtils.getLocale(request).getLanguage();
		List<School> result = schoolMapper.selectList(
				Wrappers.lambdaQuery(School.class).in(School::getId, ids).eq(School::getLanguage, language));
		return result;
	}

	@Override
	public List<School> select() {
		return null;
	}

	@Override
	public School selectById(Object o) {
		String language = RequestContextUtils.getLocale(request).getLanguage();
		School result = schoolMapper
				.selectOne(Wrappers.lambdaQuery(School.class).in(School::getId, o).eq(School::getLanguage, language));
		return result;
	}

}
