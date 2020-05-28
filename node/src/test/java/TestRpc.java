import com.alibaba.fastjson.JSON;
import com.hankcs.hanlp.dictionary.stopword.CoreStopWordDictionary;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.IndexTokenizer;
import com.mongodb.client.*;
import com.xearch.node.MongoUtil;
import com.xearch.node.RefreshDaemon;
import com.xsearch.article.lib.ArticleServiceGrpc;
import com.xsearch.article.lib.PutArticleReply;
import com.xsearch.article.lib.PutArticleRequest;
import com.xsearch.query.lib.QueryReply;
import com.xsearch.query.lib.QueryRequest;
import com.xsearch.query.lib.QueryServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.bson.Document;
import org.junit.Test;

import java.util.*;
import java.util.regex.Pattern;

public class TestRpc {
    @Test
    public void testrpc() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 1001).usePlaintext().build();
        QueryServiceGrpc.QueryServiceBlockingStub stub = QueryServiceGrpc.newBlockingStub(channel);

        List<Term> segment = CoreStopWordDictionary.apply(IndexTokenizer.segment("腾讯 科技"));
        List<String> list = new ArrayList<>();
        for (Term t : segment) {
            list.add(t.word);
        }

        QueryRequest req = QueryRequest.newBuilder().addAllTerm(list).setResultSize(10).build();
        QueryReply reply = stub.query(req);
        System.out.println(reply.getIdList());
        System.out.println(reply.getScoreList());
        System.out.println(reply.getOffsetList());
    }


    @Test
    public void testMakeIndex() {
        String url = "mongodb://root:123456@10.105.222.90:27017";
        MongoClient mongoClient = MongoClients.create(url);
        MongoDatabase db = mongoClient.getDatabase("xnode1");
        System.out.println(db.getName());
        FindIterable<Document> allIndex = db.getCollection("invertedIndex").find();

        MongoCursor<Document> iterator = allIndex.iterator();

        while (iterator.hasNext()) {
            Document curInvertedIndex = iterator.next();
            Map<String, Map<String, List<Integer>>> mergedIndex = curInvertedIndex.get("invertedIndex", Map.class);
            System.out.println(mergedIndex.keySet().size());
        }
    }

    @Test
    public void testTokenizer() {
//        List<Term> segment = CoreStopWordDictionary.apply(IndexTokenizer.segment("hi how are you? I'm fine thank you."));
//        for (Term t : segment) {
//            System.out.println(t.word);
//            System.out.println(t.nature);
//        }
        Map<Integer, Integer> m = null;
        System.out.println(m.keySet().size());


    }

    @Test
    public void testSort() {
        Map<String, Double> m = new HashMap<>();
        m.put("1", 234.15);
        m.put("2", 63.1515);
        m.put("3", 15416.2);

        List<Map.Entry<String, Double>> list = new ArrayList(m.entrySet());
        Comparator<Map.Entry<String, Double>> comparator = Comparator.comparing(Map.Entry::getValue);
        Collections.sort(list, comparator.reversed());

        System.out.println(list.get(0));


    }

    @Test
    public void testMerge() {

    }

}
