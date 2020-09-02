package com.techstar.mongodb.demo;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

/**
 * MongoDB删除操作
 */
public class delete {
    public static void main(String[] args) {
        //连接服务
        MongoClientURI uri = new MongoClientURI("mongodb://test:123456@127.0.0.1:27017/?authSource=test&authMechanism=SCRAM-SHA-1");
        MongoClient client = new MongoClient(uri);
        //连接数据库
        MongoDatabase db = client.getDatabase("test");
        //连接集合
        MongoCollection<Document> collection = db.getCollection("zips");
        //创建一条文档
        BasicDBObject query = new BasicDBObject("city", "Beijing");

        //删除符合条件的一条文档
        //collection.deleteOne(Filters.eq(query));
        //删除符合条件的多条文档
        collection.deleteMany(query);
    }
}
