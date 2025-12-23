package cool.scx.http.x.http1.request_line.request_target;

import java.net.URI;
import java.net.URISyntaxException;

/// 例: `GET /index.html?foo=1 HTTP/1.1`
///
/// 用于大多数客户端请求
public record OriginForm(String path, String query, String fragment) implements RequestTarget {

    /// 这里 origin 必定是 "/" 起始, 如果不是 那么结果是不可靠的.
    public static OriginForm of(String origin) throws URISyntaxException {
        // 我们借用 URI 来作为 解析器
        var u = new URI(origin);
        var path = u.getPath();
        var query = u.getQuery();

        // 根据 HTTP 规范, 不应该允许 fragment 但是我们这里选择支持
        var fragment = u.getFragment();

        return new OriginForm(path, query, fragment);
    }

}
