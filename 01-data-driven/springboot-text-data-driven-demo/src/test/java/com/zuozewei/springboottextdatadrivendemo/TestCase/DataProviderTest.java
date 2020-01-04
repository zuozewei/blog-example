package com.zuozewei.springboottextdatadrivendemo.TestCase;

import com.zuozewei.springboottextdatadrivendemo.paramter.TxtIterator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * 描述:
 * DataProvider 测试类
 *
 * @author zuozewei
 * @create 2019-11-23 19:56
 */

@SpringBootTest
@Slf4j
public class DataProviderTest extends AbstractTestNGSpringContextTests {

    String filePath; // 文件名

    @Parameters({"filePath"})
    @BeforeClass()
    public void beforeClass(String filePath) {
        log.info("文件路径：[{}]",filePath);
        this.filePath =  System.getProperty("user.dir") + "\\" + filePath;;
    }


    @DataProvider(name = "iterator")
    public Iterator<Object[]> iteratorDataProvider() throws IOException {
        log.info("文件路径：[{}]",filePath);
        return new TxtIterator(new File(filePath));
    }

    @Test(dataProvider = "iterator" ,description = "测试延迟提供数据")
    public void testcase2(String username,String password) throws InterruptedException {
       log.info(" username = [{}] ,password = [{}]" ,username,password );

       // 休眠2秒
       Thread.sleep(2000);
    }

}
