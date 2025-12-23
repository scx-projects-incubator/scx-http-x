package cool.scx.http.x.http1.request_line.request_target;

import dev.scx.http.uri.ScxURI;

import java.net.URI;
import java.net.URISyntaxException;

/// 例: `CONNECT example.com:443 HTTP/1.1`
///
/// 仅与 `CONNECT` 方法一起使用
public record AuthorityForm(String host, int port) implements RequestTarget {

    public static AuthorityForm of(String authority) throws URISyntaxException {
        // 我们借用 URI 来作为 解析器
        var u = new URI("scx://" + authority);

        var host = u.getHost();
        var port = u.getPort();
        var rawPath = u.getRawPath();
        var rawQuery = u.getRawQuery();
        var rawFragment = u.getRawFragment();
        var rawUserInfo = u.getRawUserInfo();

        if (host == null || host.isEmpty()) {
            throw new URISyntaxException(authority, "Invalid authority: host is missing");
        }

        if (port <= 0 || port > 65535) {
            throw new URISyntaxException(authority, "Invalid authority: port is missing or invalid");
        }

        if (!"".equals(rawPath)) {
            throw new URISyntaxException(authority, "Invalid authority: path not allowed");
        }

        if (rawQuery != null) {
            throw new URISyntaxException(authority, "Invalid authority: query not allowed");
        }

        if (rawFragment != null) {
            throw new URISyntaxException(authority, "Invalid authority: fragment not allowed");
        }

        if (rawUserInfo != null) {
            throw new URISyntaxException(authority, "Invalid authority: user-info not allowed");
        }

        return new AuthorityForm(u.getHost(), u.getPort());
    }

    @Override
    public ScxURI toScxURI() {
        return ScxURI.of()
            .host(host)
            .port(port);
    }

}
