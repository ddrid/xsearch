import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.DBObject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.UUID;

public class Crawler {
    public static void main(String[] args) throws Exception {

        //get请求中包含的参数
        String include = "data%5B%3F(target.type%3Dtopic_sticky_module)%5D.target.data%5B%3F(target.type%3Danswer)%5D.target.content%2Crelationship.is_authorized%2Cis_author%2Cvoting%2Cis_thanked%2Cis_nothelp%3Bdata%5B%3F(target.type%3Dtopic_sticky_module)%5D.target.data%5B%3F(target.type%3Danswer)%5D.target.is_normal%2Ccomment_count%2Cvoteup_count%2Ccontent%2Crelevant_info%2Cexcerpt.author.badge%5B%3F(type%3Dbest_answerer)%5D.topics%3Bdata%5B%3F(target.type%3Dtopic_sticky_module)%5D.target.data%5B%3F(target.type%3Darticle)%5D.target.content%2Cvoteup_count%2Ccomment_count%2Cvoting%2Cauthor.badge%5B%3F(type%3Dbest_answerer)%5D.topics%3Bdata%5B%3F(target.type%3Dtopic_sticky_module)%5D.target.data%5B%3F(target.type%3Dpeople)%5D.target.answer_count%2Carticles_count%2Cgender%2Cfollower_count%2Cis_followed%2Cis_following%2Cbadge%5B%3F(type%3Dbest_answerer)%5D.topics%3Bdata%5B%3F(target.type%3Danswer)%5D.target.annotation_detail%2Ccontent%2Chermes_label%2Cis_labeled%2Crelationship.is_authorized%2Cis_author%2Cvoting%2Cis_thanked%2Cis_nothelp%3Bdata%5B%3F(target.type%3Danswer)%5D.target.author.badge%5B%3F(type%3Dbest_answerer)%5D.topics%3Bdata%5B%3F(target.type%3Darticle)%5D.target.annotation_detail%2Ccontent%2Chermes_label%2Cis_labeled%2Cauthor.badge%5B%3F(type%3Dbest_answerer)%5D.topics%3Bdata%5B%3F(target.type%3Dquestion)%5D.target.annotation_detail%2Ccomment_count%3B";
        int limit = 10;

        String mongodbUrl = "mongodb://root:123456@10.105.222.90:27017";
        MongoClient mongoClient = MongoClients.create(mongodbUrl);
        MongoDatabase db = mongoClient.getDatabase("xnode1");

        for (int i = 0; i < 1; i++) {
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
                String id = UUID.randomUUID().toString().replace("-", "").toLowerCase();
                Article article = new Article(id, -1, updateTime, url, title, content);
                db.getCollection("article").insertOne(Document.parse(JSON.toJSONString(article)));

                count++;
            }
            System.out.println();
        }

    }

}
