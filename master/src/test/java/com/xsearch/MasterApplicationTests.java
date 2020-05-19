package com.xsearch;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xsearch.entity.Article;
import com.xsearch.testproto.lib.HelloReply;
import com.xsearch.testproto.lib.HelloRequest;
import com.xsearch.testproto.lib.MyServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

@SpringBootTest
class MasterApplicationTests {

//    @Autowired
//    private MongoTemplate mongoTemplate;
    @Value("${slaveNum}")
    int slaveNum;

    @Value("${articleNum}")
    int articleNum;

    @Test
    void testt() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost",1001).usePlaintext().build();
        MyServiceGrpc.MyServiceBlockingStub myServiceBlockingStub =  MyServiceGrpc.newBlockingStub(channel);

        HelloRequest req = HelloRequest.newBuilder().setName("neo").build();
        HelloReply reply = myServiceBlockingStub.sayHello(req);
        System.out.println(reply.getMessage());
    }

//    @Test
//    void crawler() throws Exception {
//
//        //get请求中包含的参数
//        String include = "data%5B%3F(target.type%3Dtopic_sticky_module)%5D.target.data%5B%3F(target.type%3Danswer)%5D.target.content%2Crelationship.is_authorized%2Cis_author%2Cvoting%2Cis_thanked%2Cis_nothelp%3Bdata%5B%3F(target.type%3Dtopic_sticky_module)%5D.target.data%5B%3F(target.type%3Danswer)%5D.target.is_normal%2Ccomment_count%2Cvoteup_count%2Ccontent%2Crelevant_info%2Cexcerpt.author.badge%5B%3F(type%3Dbest_answerer)%5D.topics%3Bdata%5B%3F(target.type%3Dtopic_sticky_module)%5D.target.data%5B%3F(target.type%3Darticle)%5D.target.content%2Cvoteup_count%2Ccomment_count%2Cvoting%2Cauthor.badge%5B%3F(type%3Dbest_answerer)%5D.topics%3Bdata%5B%3F(target.type%3Dtopic_sticky_module)%5D.target.data%5B%3F(target.type%3Dpeople)%5D.target.answer_count%2Carticles_count%2Cgender%2Cfollower_count%2Cis_followed%2Cis_following%2Cbadge%5B%3F(type%3Dbest_answerer)%5D.topics%3Bdata%5B%3F(target.type%3Danswer)%5D.target.annotation_detail%2Ccontent%2Chermes_label%2Cis_labeled%2Crelationship.is_authorized%2Cis_author%2Cvoting%2Cis_thanked%2Cis_nothelp%3Bdata%5B%3F(target.type%3Danswer)%5D.target.author.badge%5B%3F(type%3Dbest_answerer)%5D.topics%3Bdata%5B%3F(target.type%3Darticle)%5D.target.annotation_detail%2Ccontent%2Chermes_label%2Cis_labeled%2Cauthor.badge%5B%3F(type%3Dbest_answerer)%5D.topics%3Bdata%5B%3F(target.type%3Dquestion)%5D.target.annotation_detail%2Ccomment_count%3B";
//        int limit = 10;
//
//        //爬取200篇
//        for (int i = 0; i < 20; i++) {
//            int offset = 10 * i;
//            URL targetUrl = new URL("https://www.zhihu.com/api/v4/topics/19556664/feeds/essence?include=" + include + "&offset=" + offset + "&limit=" + limit);
//            URLConnection connection = targetUrl.openConnection();
//            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//            String line = reader.readLine();
//            JSONObject jsonObject = JSON.parseObject(line);
//            List<JSONObject> data = jsonObject.getJSONArray("data").toJavaList(JSONObject.class);
//
//            int count = 0;
//
//            // 遍历json找出标题，内容，构成对象后存入mongodb
//            for (JSONObject o : data) {
//                System.out.println(offset + count);
//                JSONObject question = o.getJSONObject("target").getJSONObject("question");
//                int updateTime;
//                String title;
//                String url;
//                if (question == null) {
//                    //知乎专栏
//                    title = o.getJSONObject("target").getString("title");
//                    url = o.getJSONObject("target").getString("url");
//                    updateTime = o.getJSONObject("target").getInteger("updated");
//                } else {
//                    //问答类
//                    title = question.getString("title");
//                    url = "https://zhihu.com/question/" + question.getString("id") +
//                            "/answer/" + o.getJSONObject("target").getString("id");
//                    updateTime = o.getJSONObject("target").getInteger("updated_time");
//                }
//
//                String content = o.getJSONObject("target").getString("content");
//                mongoTemplate.insert(new Article(offset + count, updateTime, url, title, content));
//                count++;
//            }
//            System.out.println();
//        }
//
//    }

//    @Test
//    void makeIndex() throws Exception {
//
//        long startTime = System.currentTimeMillis();
//
//        List<Future<Map<String, Map<Integer, List<Integer>>>>> futureList = new ArrayList<>();
//
//        Map<String, Map<Integer, List<Integer>>> mergedIndex;
//
//        int articleNumPerSlave = articleNum / slaveNum;
//
//        for (int i = 0; i < slaveNum - 1; i++) {
//            futureList.add(makeIndexAsync.getIndexResult(articleNumPerSlave * i, articleNumPerSlave * (i + 1)));
//        }
//
//        futureList.add(makeIndexAsync.getIndexResult(articleNumPerSlave * (slaveNum - 1), articleNum));
//
//        while (true) {
//            boolean isDone = true;
//            for (int i = 0; i < slaveNum; i++) {
//                if (!futureList.get(i).isDone()) {
//                    isDone = false;
//                    break;
//                }
//            }
//            if (isDone) {
//                mergedIndex = futureList.get(0).get();
//                for (int i = 1; i < slaveNum; i++) {
//                    // word1 -> {id1 -> [tf1, off11, off12...], id2 -> [tf2, off21, off22...], ... }
//                    Map<String, Map<Integer, List<Integer>>> cur = futureList.get(i).get();
//                    for (String key : cur.keySet()) {
//                        if (mergedIndex.get(key) != null) {
//                            mergedIndex.get(key).putAll(cur.get(key));
//                        } else {
//                            mergedIndex.put(key, cur.get(key));
//                        }
//                    }
//                }
//                break;
//            }
//        }
//
//        System.out.println("单词总数: " + mergedIndex.keySet().size());
//
//        long endTime = System.currentTimeMillis();
//        System.out.println("用时: " + (double) (endTime - startTime) / 1000 + " 秒");
//
//    }

}
