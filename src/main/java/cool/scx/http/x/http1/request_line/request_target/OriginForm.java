package cool.scx.http.x.http1.request_line.request_target;

import java.net.URI;
import java.net.URISyntaxException;

/// 例: `GET /index.html?foo=1 HTTP/1.1`
///
/// 用于大多数客户端请求
public record OriginForm(String path, String query, String fragment) implements RequestTarget {

    public static OriginForm of(String origin) throws URISyntaxException {
        // 我们借用 URI 来作为 解析器
        var u = new URI(origin);
        var path = u.getPath();
        var query = u.getQuery();
        var fragment = u.getFragment();

        return new OriginForm(path, query, fragment);
    }

}
