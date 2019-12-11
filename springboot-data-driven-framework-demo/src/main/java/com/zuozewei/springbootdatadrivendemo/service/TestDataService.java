package com.zuozewei.springbootdatadrivendemo.service;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * 描述: TestDataService
 *
 * @author zuozewei
 * @create 2019-11-21 18:00
 */

public interface TestDataService {

    // 自定义查询1
    List<LinkedHashMap<String, Object>> selectBysql(String sql);

    // 自定义查询2
    List<LinkedHashMap<String, Object>> selectBySlowsql(String sql);
}

