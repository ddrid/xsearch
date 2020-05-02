package com.xsearch;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.IndexTokenizer;
import com.xsearch.entity.Article;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootTest
class XsearchApplicationTests {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    void crawler() throws Exception {

        //get请求中包含的参数
        String include = "data%5B%3F(target.type%3Dtopic_sticky_module)%5D.target.data%5B%3F(target.type%3Danswer)%5D.target.content%2Crelationship.is_authorized%2Cis_author%2Cvoting%2Cis_thanked%2Cis_nothelp%3Bdata%5B%3F(target.type%3Dtopic_sticky_module)%5D.target.data%5B%3F(target.type%3Danswer)%5D.target.is_normal%2Ccomment_count%2Cvoteup_count%2Ccontent%2Crelevant_info%2Cexcerpt.author.badge%5B%3F(type%3Dbest_answerer)%5D.topics%3Bdata%5B%3F(target.type%3Dtopic_sticky_module)%5D.target.data%5B%3F(target.type%3Darticle)%5D.target.content%2Cvoteup_count%2Ccomment_count%2Cvoting%2Cauthor.badge%5B%3F(type%3Dbest_answerer)%5D.topics%3Bdata%5B%3F(target.type%3Dtopic_sticky_module)%5D.target.data%5B%3F(target.type%3Dpeople)%5D.target.answer_count%2Carticles_count%2Cgender%2Cfollower_count%2Cis_followed%2Cis_following%2Cbadge%5B%3F(type%3Dbest_answerer)%5D.topics%3Bdata%5B%3F(target.type%3Danswer)%5D.target.annotation_detail%2Ccontent%2Chermes_label%2Cis_labeled%2Crelationship.is_authorized%2Cis_author%2Cvoting%2Cis_thanked%2Cis_nothelp%3Bdata%5B%3F(target.type%3Danswer)%5D.target.author.badge%5B%3F(type%3Dbest_answerer)%5D.topics%3Bdata%5B%3F(target.type%3Darticle)%5D.target.annotation_detail%2Ccontent%2Chermes_label%2Cis_labeled%2Cauthor.badge%5B%3F(type%3Dbest_answerer)%5D.topics%3Bdata%5B%3F(target.type%3Dquestion)%5D.target.annotation_detail%2Ccomment_count%3B";
        int limit = 10;

        //爬取200篇
        for (int i = 0; i < 20; i++) {
            int offset = 10 * i;
            URL targetUrl = new URL("https://www.zhihu.com/api/v4/topics/19556664/feeds/essence?include=" + include + "&offset=" + offset + "&limit=" + limit);
            URLConnection connection = targetUrl.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line = reader.readLine();
            JSONObject jsonObject = JSON.parseObject(line);
            List<JSONObject> data = jsonObject.getJSONArray("data").toJavaList(JSONObject.class);

            int count = 0;

            // 遍历json找出标题，内容，构成对象后存入mongodb
            for (JSONObject o : data) {
                System.out.println(offset + count);
                JSONObject question = o.getJSONObject("target").getJSONObject("question");
                int updateTime;
                String title;
                String url;
                if (question == null) {
                    //知乎专栏
                    title = o.getJSONObject("target").getString("title");
                    url = o.getJSONObject("target").getString("url");
                    updateTime = o.getJSONObject("target").getInteger("updated");
                } else {
                    //问答类
                    title = question.getString("title");
                    url = "https://zhihu.com/question/" + question.getString("id") +
                            "/answer/" + o.getJSONObject("target").getString("id");
                    updateTime = o.getJSONObject("target").getInteger("updated_time");
                }

                String content = o.getJSONObject("target").getString("content");
                mongoTemplate.insert(new Article(offset + count, updateTime, url, title, content));
                count++;
            }
            System.out.println();
        }

    }

    @Test
    void Indexer() {

        long startTime = System.currentTimeMillis();

        // word1 -> {id1 -> [tf1, off11, off12...], id2 -> [tf2, off21, off22...], ... }
        Map<String, Map<Integer, List<Integer>>> index = new HashMap<>();

        for (int i = 0; i < 200; i++) {
            System.out.println(i);
            Query query = new Query(Criteria.where("index").is(i));
            Article article = mongoTemplate.findOne(query, Article.class);
            assert article != null;
            String content = article.getContent();
            List<Term> segment = IndexTokenizer.segment(content);
            for (Term term : segment) {
                String regex = ".*[<>,.]+.*";
                //去除html标签以及特殊符号 <>,.
                if (!Pattern.compile(regex).matcher(term.word).matches()) {
                    // 不存在该词项
                    index.computeIfAbsent(term.word, k -> new HashMap<>());

                    //{id1 -> [tf1, off11, off12...], id2 -> [tf2, off21, off22...], ... }
                    Map<Integer, List<Integer>> indexMap = index.get(term.word);

                    //词项内不存在这篇文章的id
                    if (indexMap.get(i) == null) {
                        List<Integer> indexList = new ArrayList<>();
                        //先将tf置0
                        indexList.add(0);
                        indexMap.put(i, indexList);
                    }

                    List<Integer> indexList = indexMap.get(i);

                    // 将tf值加一
                    indexList.set(0, indexList.get(0) + 1);

                    // 将该词在文中的offset追加至list尾部
                    indexList.add(term.offset);
                }
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.println("用时：" + (double) (endTime - startTime) / 1000 + " 秒");

    }

    @Test
    void muddle() {
        long c = 1478249926;
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
        String dateStr = dateformat.format(c * 1000);
        System.out.println(dateStr);
    }

}
