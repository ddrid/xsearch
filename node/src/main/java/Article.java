
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Article {

    int index;

    int segment;

    int updateTime;

    String url;

    String title;

    String content;
}
