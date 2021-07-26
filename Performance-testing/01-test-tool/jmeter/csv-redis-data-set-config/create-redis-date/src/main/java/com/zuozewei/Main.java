package com.zuozewei;

import redis.clients.jedis.Jedis;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

/**
 * @author zuozewei
 */


public class Main {
    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
        Main main = new Main();
//        main.createCsvDate();
        main.createRedisDate();
    }

    void createCsvDate() throws FileNotFoundException, UnsupportedEncodingException {
        int maximum = 1000000;
        PrintWriter writer = new PrintWriter("d:/redis-csv-compare.csv","UTF-8");
        for (int i=1;i<=maximum;i++){
            writer.println("name" + i + ",age" + i +",addresss" + i);
        }
        writer.close();
    }

    void createRedisDate(){
        //连接本地的redis,port如果没有修改可以不用传
        Jedis jedis = new Jedis("172.16.106.237");
//        System.out.println(jedis.getClient().getPort());
//        System.out.println("连接本地的Redis服务器成功");
//        //查看服务是否运行
//        System.out.println("服务正在运行：" + jedis.ping());
//
        int maximum = 1000000;

        for (int i=1;i<=maximum;i++){
            jedis.lpush("user-row","name" + i + ",age" + i +",addresss" + i);
        }
        jedis.close();
    }
}
