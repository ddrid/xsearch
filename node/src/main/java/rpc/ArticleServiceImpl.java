package rpc;

import com.mongodb.client.MongoDatabase;
import com.xsearch.article.lib.ArticleServiceGrpc;
import com.xsearch.article.lib.PutArticleReply;
import com.xsearch.article.lib.PutArticleRequest;
import io.grpc.stub.StreamObserver;
import org.bson.Document;
import util.MongoUtil;

public class ArticleServiceImpl extends ArticleServiceGrpc.ArticleServiceImplBase {

    @Override
    public void putArticle(PutArticleRequest request, StreamObserver<PutArticleReply> responseObserver) {
        PutArticleReply reply = PutArticleReply.newBuilder().setMessage("success").build();
        System.out.println(request);
//        MongoDatabase db = MongoUtil.getConnection();
//
//        db.getCollection("test").insertOne(new Document("adfasfd","adsfasdfsddf"));
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}

