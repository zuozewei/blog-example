package com.zuozewei.springbootdatabackuprecoverydemo.service;

/**
 * 描述: TestDataService
 *
 * @author zuozewei
 * @create 2019-11-21 18:00
 */

public interface TestDataService {

   /**
    * 准备数据库数据
    * @param tableName
    */
   void createTableData(String tableName);


   /**
    * 清理数据库数据
    * @param tableName
    */
   void recycleTableData(String tableName);
}

