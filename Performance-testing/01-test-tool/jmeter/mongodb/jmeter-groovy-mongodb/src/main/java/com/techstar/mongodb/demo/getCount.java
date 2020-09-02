package com.techstar.mongodb.demo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

/**
 * MongoDB获取集合总数
 */
public class getCount {
    public static void main(String[] args) {
        //连接到MongoDB服务
        MongoClientURI uri = new MongoClientURI("mongodb://test:123456@127.0.0.1:27017/?authSource=test&authMechanism=SCRAM-SHA-1");
        MongoClient client = new MongoClient(uri);
        //连接到数据库
        MongoDatabase db = client.getDatabase("test");
        //连接到集合
        MongoCollection<Document> collection = db.getCollection("zips");
        //获取到集合总数
        long count = collection.countDocuments();
        //count转换字符串打印
        String result = String.valueOf(count);
        System.out.println(result);

    }




}
