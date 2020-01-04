package com.zuozewei.springbootdatabackuprecoverydemo.service.impl;

import com.zuozewei.springbootdatabackuprecoverydemo.db.manual.mapper.TestDataMapper;
import com.zuozewei.springbootdatabackuprecoverydemo.service.TestDataService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 描述: TestDataService 实现类
 *
 * @author zuozewei
 * @create 2019-11-21 16:04
 */

@Service
public class TestDataServiceImpl implements TestDataService {

    @Resource
    private TestDataMapper testDataMapper;

    /**
     * 准备数据库数据
     * @param tableName
     */
    @Override
    public void createTableData(String tableName) {
        // 新表名
        String newTableName = tableName + "_bak";
        // 源表名
        String originalTableName = tableName;

        // 创建测试表并复制数据
        testDataMapper.createNewTableAndInsertData(newTableName,originalTableName);
    }


    /**
     * 清理数据库数据
     * @param tableName
     */
    @Override
    public void recycleTableData(String tableName) {
        // 新表名
        String newTableName = tableName ;
        // 源表名
        String originalTableName = tableName + "_bak";

        // 删除测试表
        testDataMapper.dropTable(tableName);
        // 恢复备份表
        testDataMapper.alterTableName(originalTableName,newTableName);
    }
}
