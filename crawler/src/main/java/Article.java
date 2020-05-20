
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Article{

    String id;

    int segment;

    int updateTime;

    String url;

    String title;

    String content;
}
