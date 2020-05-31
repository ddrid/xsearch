package com.xearch.node.util;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 工具类
 * 用于连接 mongodb
 */
public class MongoUtil {

    public static MongoDatabase getConnection() {
        Logger mongoLogger = Logger.getLogger( "org.mongodb.driver" );
        mongoLogger.setLevel(Level.SEVERE);
        String url = "mongodb://root:123456@10.105.222.90:27017";
        MongoClient mongoClient = MongoClients.create(url);
        return mongoClient.getDatabase("xnode" + System.getProperty("id"));
    }
}
