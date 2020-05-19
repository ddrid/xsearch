package util;

import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import java.util.ArrayList;
import java.util.List;

public class MongoUtil {
    public static MongoDatabase getConnection() {
        String url = "mongodb://root:123456@10.105.222.90:27017";
        MongoClient mongoClient = MongoClients.create(url);
        return mongoClient.getDatabase("xnode" + System.getProperty("id"));
    }
}
