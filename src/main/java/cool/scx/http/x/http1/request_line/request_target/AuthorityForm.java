package cool.scx.http.x.http1.request_line.request_target;

import dev.scx.http.uri.ScxURI;

import java.net.URI;
import java.net.URISyntaxException;

/// 例: `CONNECT example.com:443 HTTP/1.1`
///
/// 仅与 `CONNECT` 方法一起使用
public record AuthorityForm(String host, int port) implements RequestTarget {

    public static AuthorityForm of(String authority) throws URISyntaxException {
        var colon = authority.lastIndexOf(":");
        if (colon == -1) {
            throw new URISyntaxException(authority, "Invalid authority");
        }
        var hostStr = authority.substring(0, colon);
        var portStr = authority.substring(colon + 1);

        String host;
        int port;

        // 我们需要校验 portStr 必须存在 + 是一个数组 + 范围在 1 - 65535 中
        if (portStr.isEmpty()) {
            throw new URISyntaxException(authority, "port is missing");
        }
        try {
            port = Integer.parseInt(portStr);
            if (port < 1 || port > 65535) {
                throw new URISyntaxException(authority, "Invalid port");
            }
        } catch (NumberFormatException e) {
            throw new URISyntaxException(authority, "Invalid port");
        }

        // 我们需要校验 host 是一个合法的 host
        if (hostStr.isEmpty()) {
            throw new URISyntaxException(authority, "Host is missing");
        }

        return new AuthorityForm(host, port);
    }

    @Override
    public ScxURI toScxURI() {
        return ScxURI.of()
            .host(host)
            .port(port);
    }

}
