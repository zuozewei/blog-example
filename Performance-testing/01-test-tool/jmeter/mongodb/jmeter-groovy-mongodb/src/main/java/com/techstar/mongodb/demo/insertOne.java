package com.techstar.mongodb.demo;

import com.mongodb.BasicDBList;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

/**
 * MongoDB插入操作
 */
public class insertOne {
    public static void main(String[] args) {
        //连接服务
        MongoClientURI uri = new MongoClientURI("mongodb://test:123456@127.0.0.1:27017/?authSource=test&authMechanism=SCRAM-SHA-1");
        MongoClient client = new MongoClient(uri);
        //连接数据库
        MongoDatabase db = client.getDatabase("test");
        //连接集合
        MongoCollection<Document> collection = db.getCollection("zips");
        //创建空文档
        Document object = new Document();
        //创建包含经纬度的数组
        BasicDBList location = new BasicDBList();
        location.add(32.211100D);
        location.add(44.251500D);
        //保存到文档中
        object.append("city","Beijing");
        object.append("loc",location);
        object.append("pop",300000);
        object.append("state","IS");
        //插入单条文档
        collection.insertOne(object);
    }
}
