package com.zuozewei.springbootdatadrivendemo.demo;

import com.zuozewei.springbootdatadrivendemo.db.auto.model.Coffee;
import com.zuozewei.springbootdatadrivendemo.db.auto.model.CoffeeExample;
import com.zuozewei.springbootdatadrivendemo.service.CoffeeService;
import com.zuozewei.springbootdatadrivendemo.service.TestDataService;
import com.zuozewei.springbootdatadrivendemo.utils.FastJsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.joda.money.Money;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootTest
@Slf4j
public class TestMapperService extends AbstractTestNGSpringContextTests {

    private String sql; // SQL参数

	@Autowired
    private CoffeeService coffeeService;

    @Autowired
    private TestDataService testDataService;

    @Parameters({"sql"})
    @BeforeClass
    public void beforeClass(String sql) {
        this.sql = sql;
    }

    /**
     * XML中的SQL决定了执行什么用例, 执行多少条用例, SQL的搜索结果为需要测试的测试用例
     */

    @DataProvider(name = "testData")
    private Object[][] getData() {
        List<LinkedHashMap<String, Object>> results = testDataService.selectBysql(sql);
        Object[][] objects = new Object[results.size()][];
        for (int i = 0; i < results.size(); i++) {
            objects[i] = new Object[]{results.get(i)};
        }
        return objects;

    }

    @Test(dataProvider = "testData",description = "测试demo")
    public void testSelect(Map<String, String> data) throws InterruptedException {

//        Coffee espresso = new Coffee()
//                .withName("zuozewei")
//                .withPrice(Money.of(CurrencyUnit.of("CNY"), 20.0))
//                .withCreateTime(new Date())
//                .withUpdateTime(new Date());
//        coffeeService.addCoffee(espresso);
//
//        Coffee latte = new Coffee()
//                .withName("zee")
//                .withPrice(Money.of(CurrencyUnit.of("CNY"), 30.0))
//                .withCreateTime(new Date())
//                .withUpdateTime(new Date());
//        coffeeService.addCoffee(latte);'

        // 参数取值
        String parameters = FastJsonUtils.fromString(data.get("Parameters"),"Parameters");

        // 封装查询条件
        CoffeeExample example = new CoffeeExample();

        //创建一个 Criteria，来拼装查询条件
        example.createCriteria().andNameEqualTo(parameters);

        // 查询数据
        List<Coffee> list = coffeeService.selectCoffeeFromDs(example);

        // 循环打印
        list.forEach(e -> log.info("selectByExample: {}", e));

        // 筛选指定属性
        List<Money> moneys = list.stream().map(Coffee::getPrice).collect(Collectors.toList());
        log.info( moneys.get(0).toString() );

        // 断言结果
        Assert.assertEquals(data.get("expected"),moneys.get(0).toString());

    }

    @Test
    public void testTestData() {
        String sqlString = "SELECT * FROM emp WHERE empno = 403345;";

        //调用
        List<LinkedHashMap<String, Object>> names = testDataService.selectBySlowsql(sqlString);
        names.forEach(name ->  log.info("selectBySlowsql: {}", name));

    }

}