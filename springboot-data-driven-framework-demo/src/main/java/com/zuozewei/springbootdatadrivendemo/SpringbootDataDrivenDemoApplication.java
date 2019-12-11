package com.zuozewei.springbootdatadrivendemo;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication(exclude = DruidDataSourceAutoConfigure.class)
@Slf4j
@MapperScan("com.zuozewei.springbootdatadrivendemo.db")
public class SpringbootDataDrivenDemoApplication  implements ApplicationRunner {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootDataDrivenDemoApplication.class, args);
        log.info("程序启动！");
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
		//generateArtifacts();
        log.info("启动generateArtifacts");
    }

    /**
     * 执行MyBatisGenerator
     * @throws Exception
     */
    private void generateArtifacts() throws Exception {
        List<String> warnings = new ArrayList<>();
        ConfigurationParser cp = new ConfigurationParser(warnings);
        Configuration config = cp.parseConfiguration(
                this.getClass().getResourceAsStream("/generatorConfig.xml"));
        DefaultShellCallback callback = new DefaultShellCallback(true);
        MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, callback, warnings);
        myBatisGenerator.generate(null);
    }

}
