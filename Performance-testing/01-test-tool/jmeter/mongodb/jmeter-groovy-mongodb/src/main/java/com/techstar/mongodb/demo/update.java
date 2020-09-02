package com.techstar.mongodb.demo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import com.mongodb.client.MongoCollection;

public class update {
    public static void main(String[] args) {

        try {
            //连接服务
            MongoClientURI uri = new MongoClientURI("mongodb://test:123456@127.0.0.1:27017/?authSource=test&authMechanism=SCRAM-SHA-1");
            MongoClient client = new MongoClient(uri);
            //连接数据库
            MongoDatabase db = client.getDatabase("test");
            //连接集合
            MongoCollection<Document> collection = db.getCollection("zips");

            //创建条件文档
            Document squery = new Document("city", "BABYLON");
            //创建待更新文档
            Document uquery = new Document("city", "Shanghai");
            Document dresult = new Document("$set", uquery);
            //更新所有符合条件的文档
            collection.updateMany(squery, dresult);
        }catch(Exception e){
            System.err.println(e.getClass()+":"+e.getMessage());
        }
    }
}
