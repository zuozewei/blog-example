package com.zuozewei.springbootdatadrivendemo.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.zuozewei.springbootdatadrivendemo.db.manual.mapper.TestDataMapper;
import com.zuozewei.springbootdatadrivendemo.service.TestDataService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 描述: 参数化自定义查询实现类
 *
 * @author zuozewei
 * @create 2019-11-21 16:04
 */

@Service
public class TestDataServiceImpl implements TestDataService {

    @Resource
    private TestDataMapper testDataMapper;

    @DS("mysql_1")
    @Override
    public List<LinkedHashMap<String, Object>> selectBysql(String sql) {
        return testDataMapper.selectBysql(sql);
    }

    @DS("mysql_2")
    @Override
    public List<LinkedHashMap<String, Object>> selectBySlowsql(String sql) {
        return testDataMapper.selectBySlowsql(sql);
    }
}
