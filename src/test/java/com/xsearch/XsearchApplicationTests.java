package com.xsearch;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import com.xsearch.entity.Article;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

@SpringBootTest
class XsearchApplicationTests {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    void contextLoads() throws Exception {

        String include = "data%5B%3F(target.type%3Dtopic_sticky_module)%5D.target.data%5B%3F(target.type%3Danswer)%5D.target.content%2Crelationship.is_authorized%2Cis_author%2Cvoting%2Cis_thanked%2Cis_nothelp%3Bdata%5B%3F(target.type%3Dtopic_sticky_module)%5D.target.data%5B%3F(target.type%3Danswer)%5D.target.is_normal%2Ccomment_count%2Cvoteup_count%2Ccontent%2Crelevant_info%2Cexcerpt.author.badge%5B%3F(type%3Dbest_answerer)%5D.topics%3Bdata%5B%3F(target.type%3Dtopic_sticky_module)%5D.target.data%5B%3F(target.type%3Darticle)%5D.target.content%2Cvoteup_count%2Ccomment_count%2Cvoting%2Cauthor.badge%5B%3F(type%3Dbest_answerer)%5D.topics%3Bdata%5B%3F(target.type%3Dtopic_sticky_module)%5D.target.data%5B%3F(target.type%3Dpeople)%5D.target.answer_count%2Carticles_count%2Cgender%2Cfollower_count%2Cis_followed%2Cis_following%2Cbadge%5B%3F(type%3Dbest_answerer)%5D.topics%3Bdata%5B%3F(target.type%3Danswer)%5D.target.annotation_detail%2Ccontent%2Chermes_label%2Cis_labeled%2Crelationship.is_authorized%2Cis_author%2Cvoting%2Cis_thanked%2Cis_nothelp%3Bdata%5B%3F(target.type%3Danswer)%5D.target.author.badge%5B%3F(type%3Dbest_answerer)%5D.topics%3Bdata%5B%3F(target.type%3Darticle)%5D.target.annotation_detail%2Ccontent%2Chermes_label%2Cis_labeled%2Cauthor.badge%5B%3F(type%3Dbest_answerer)%5D.topics%3Bdata%5B%3F(target.type%3Dquestion)%5D.target.annotation_detail%2Ccomment_count%3B";
        int limit = 10;
        for (int i = 0; i < 10; i++) {
            System.out.println(i);
            int offset = 10 * i;
            URL url = new URL("https://www.zhihu.com/api/v4/topics/19556664/feeds/essence?include=" + include + "&offset=" + offset + "&limit=" + limit);
            URLConnection connection = url.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line = reader.readLine();
            JSONObject jsonObject = JSON.parseObject(line);
            List<JSONObject> data = jsonObject.getJSONArray("data").toJavaList(JSONObject.class);
            int count = 0;
            for (JSONObject o : data) {
                System.out.println(offset + count);
                System.out.println(o);
                JSONObject question = o.getJSONObject("target").getJSONObject("question");
                String title = question == null ? o.getJSONObject("target").getString("title") : question.getString("title");
                String content = o.getJSONObject("target").getString("content");
                mongoTemplate.insert(new Article(offset + count, title, content));
                count++;
            }
            System.out.println();
        }

    }

}
