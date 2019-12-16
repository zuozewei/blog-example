package com.zuozewei.springbootdatabackuprecoverydemo.demo;

import com.zuozewei.springbootdatabackuprecoverydemo.db.auto.model.Coffee;
import com.zuozewei.springbootdatabackuprecoverydemo.db.auto.model.CoffeeExample;
import com.zuozewei.springbootdatabackuprecoverydemo.service.CoffeeService;
import com.zuozewei.springbootdatabackuprecoverydemo.service.TestDataService;
import lombok.extern.slf4j.Slf4j;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
@Slf4j
public class TestMapperService extends AbstractTestNGSpringContextTests {

    private String tableName = "t_coffee"; //表名

	@Autowired
    private CoffeeService coffeeService;

    @Autowired
    private TestDataService testDataService;

    @BeforeMethod(description = "备份及准备测试数据")
    public void beforeMethod() {
        testDataService.createTableData(tableName);
    }

    @Test(description = "测试demo")
    public void testSelect() throws InterruptedException {

        // 插入数据
        Coffee espresso = new Coffee()
                .withName("zuozewei")
                .withPrice(Money.of(CurrencyUnit.of("CNY"), 20.0))
                .withCreateTime(new Date())
                .withUpdateTime(new Date());
        coffeeService.addCoffee(espresso);

        CoffeeExample example = new CoffeeExample();

        // 指定查询条件
        example.createCriteria().andNameEqualTo("zuozewei");

        // 查询数据
        List<Coffee> list = coffeeService.selectCoffeeFromDs(example);

        list.forEach(e -> log.info("selectByExample: {}", e));

        // 筛选指定属性
        List<Money> moneys = list.stream().map(Coffee::getPrice).collect(Collectors.toList());
        log.info( moneys.get(0).toString() );

        // 断言结果
        Assert.assertEquals("CNY 20.00",moneys.get(0).toString());

    }

    @AfterMethod(description = "清理及恢复数据")
    public void afterMethod() {
        testDataService.recycleTableData(tableName);
    }

}