package cool.scx.http.x.http1.request_line.request_target;

import dev.scx.http.uri.ScxURI;

import java.net.URI;
import java.net.URISyntaxException;

/// 例: `GET /index.html?foo=1 HTTP/1.1`
///
/// 用于大多数客户端请求
///
/// - 所有字段和 [ScxURI] 一样都是 存储的 "原始未编码" 值, 所以可以直接用于创建 [ScxURI]
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

    @Override
    public ScxURI toScxURI() {
        return ScxURI.of()
            .path(path)
            .query(query)
            .fragment(fragment);
    }

}
