package com.zuozewei.springbootdatabackuprecoverydemo.service.impl;

import com.zuozewei.springbootdatabackuprecoverydemo.db.auto.mapper.CoffeeMapper;
import com.zuozewei.springbootdatabackuprecoverydemo.db.auto.model.Coffee;
import com.zuozewei.springbootdatabackuprecoverydemo.db.auto.model.CoffeeExample;
import com.zuozewei.springbootdatabackuprecoverydemo.service.CoffeeService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 描述: CoffeeService  实现类
 *
 * @author zuozewei
 * @create 2019-11-21 18:00
 */

@Service
public class CoffeeServiceImpl implements CoffeeService {

      @Resource
      private CoffeeMapper coffeeMapper;

      @Override
      public int addCoffee(Coffee coffee) {
        return coffeeMapper.insert(coffee);
      }

      @Override
      public List selectCoffeeFromDs(CoffeeExample coffeeExample) throws InterruptedException {
        return coffeeMapper.selectByExample(coffeeExample);
      }

    }

