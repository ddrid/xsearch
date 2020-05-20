import com.alibaba.fastjson.JSON;
import com.mongodb.client.*;
import com.xearch.node.MongoUtil;
import com.xearch.node.RefreshDaemon;
import com.xsearch.article.lib.ArticleServiceGrpc;
import com.xsearch.article.lib.PutArticleReply;
import com.xsearch.article.lib.PutArticleRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.bson.Document;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class TestRpc {
    @Test
    public void testrpc() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 1001).usePlaintext().build();
        ArticleServiceGrpc.ArticleServiceBlockingStub stub = ArticleServiceGrpc.newBlockingStub(channel);

        PutArticleRequest req = PutArticleRequest.newBuilder().setId("22dew238e28d").setTitle("just a test").build();
        PutArticleReply reply = stub.putArticle(req);
        System.out.println(reply.getMessage());
    }


    @Test
    public void testMakeIndex() {
        String url = "mongodb://root:123456@10.105.222.90:27017";
        MongoClient mongoClient = MongoClients.create(url);
        MongoDatabase db =  mongoClient.getDatabase("xnode1");
        System.out.println(db.getName());
        FindIterable<Document> allIndex = db.getCollection("invertedIndex").find();

        MongoCursor<Document> iterator = allIndex.iterator();

        while (iterator.hasNext()) {
            Document curInvertedIndex = iterator.next();
            Map<String, Map<String, List<Integer>>> mergedIndex = curInvertedIndex.get("invertedIndex",Map.class);
            System.out.println(mergedIndex.keySet().size());
        }


    }
}
