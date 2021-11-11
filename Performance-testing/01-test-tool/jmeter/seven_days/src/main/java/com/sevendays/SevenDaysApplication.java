package com.sevendays;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan(value = "com.sevendays.mapper")
@SpringBootApplication
public class SevenDaysApplication {

    public static void main(String[] args) {
        SpringApplication.run(SevenDaysApplication.class, args);
    }

}
