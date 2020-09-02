package com.techstar.mongodb.demo;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class select {
    public static void main(String[] args) {

        //连接服务
        MongoClientURI uri = new MongoClientURI("mongodb://test:123456@127.0.0.1:27017/?authSource=test&authMechanism=SCRAM-SHA-1");
        MongoClient client = new MongoClient(uri);
        //连接数据库
        MongoDatabase db = client.getDatabase("test");
        //连接集合
        MongoCollection<Document> collection = db.getCollection("zips");
        //创建文档
        BasicDBObject query = new BasicDBObject("city", "BABYLON");
        //检索所有符合文件的所有文档
        /**
         * 1.按条件获取迭代器
         * 2.获取到游标
         * 3.通过游标遍历检索出文档集合
         */
        FindIterable<Document> findIterable = collection.find(query);
        MongoCursor<Document> mongoCursor = findIterable.iterator();
        while (mongoCursor.hasNext()){
            System.out.println(mongoCursor.next());
        }

    }
}
