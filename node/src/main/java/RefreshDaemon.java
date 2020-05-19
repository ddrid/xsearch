import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import util.MongoUtil;

public class RefreshDaemon {

    private MongoDatabase db = MongoUtil.getConnection();

    public void start() throws Exception {
        while (true) {
            Thread.sleep(1000);
            System.out.println("test");
            commit();
        }
    }

    public void commit() {
        Bson filter = Filters.eq("segment", "-1");
        MongoCollection<Document> collection = db.getCollection("test");
        FindIterable<Article> findIterable = collection.find(filter, Article.class);
        for (Article article : findIterable) {
            System.out.println(article);
        }
    }

}
