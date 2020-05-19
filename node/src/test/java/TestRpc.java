import com.xsearch.article.lib.ArticleServiceGrpc;
import com.xsearch.article.lib.PutArticleReply;
import com.xsearch.article.lib.PutArticleRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.Test;

public class TestRpc {
    @Test
    public void testrpc () {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost",1001).usePlaintext().build();
        ArticleServiceGrpc.ArticleServiceBlockingStub stub =  ArticleServiceGrpc.newBlockingStub(channel);

        PutArticleRequest req = PutArticleRequest.newBuilder().setIndex(123).setTitle("just a test").build();
        PutArticleReply reply = stub.putArticle(req);
        System.out.println(reply.getMessage());
    }
}
