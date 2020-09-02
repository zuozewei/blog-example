package com.zuozewei.springbootdatadrivendemo.db.manual.mapper;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * 描述:
 * 自定义sql查询
 *
 * @author zuozewei
 * @create 2019-11-21 21:18
 */

public interface TestDataMapper {

	// 自定义sql查询1
	List<LinkedHashMap<String, Object>> selectBysql(String sql);

	// 自定义sql查询2
	List<LinkedHashMap<String, Object>> selectBySlowsql(String sql);

}