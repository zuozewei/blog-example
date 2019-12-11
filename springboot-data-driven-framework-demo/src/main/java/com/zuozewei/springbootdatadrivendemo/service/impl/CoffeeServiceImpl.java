package com.zuozewei.springbootdatadrivendemo.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.zuozewei.springbootdatadrivendemo.db.auto.mapper.CoffeeMapper;
import com.zuozewei.springbootdatadrivendemo.db.auto.model.Coffee;
import com.zuozewei.springbootdatadrivendemo.db.auto.model.CoffeeExample;
import com.zuozewei.springbootdatadrivendemo.service.CoffeeService;
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
@DS("h2")
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

