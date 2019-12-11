package com.zuozewei.springbootdatadrivendemo.service;


import com.zuozewei.springbootdatadrivendemo.db.auto.model.Coffee;
import com.zuozewei.springbootdatadrivendemo.db.auto.model.CoffeeExample;

import java.util.List;

/**
 * 描述: coffee Service
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
