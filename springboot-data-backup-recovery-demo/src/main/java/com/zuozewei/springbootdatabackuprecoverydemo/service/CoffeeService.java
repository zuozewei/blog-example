package com.zuozewei.springbootdatabackuprecoverydemo.service;


import com.zuozewei.springbootdatabackuprecoverydemo.db.auto.model.Coffee;
import com.zuozewei.springbootdatabackuprecoverydemo.db.auto.model.CoffeeExample;

import java.util.List;

/**
 * 描述: coffeeService
 *
 * @author zuozewei
 * @create 2019-11-21 18:00
 */

public interface CoffeeService {

  // 插入
  int addCoffee(Coffee coffee);

  // 查询
  List selectCoffeeFromDs(CoffeeExample coffeeExample) throws InterruptedException;

}
