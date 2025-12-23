package cool.scx.http.x;

import cool.scx.http.x.http1.request_line.RequestTargetForm;
import dev.scx.http.uri.ScxURI;
import dev.scx.http.uri.ScxURIWritable;

import java.net.InetSocketAddress;

/// HttpSchemeHelper (内部工具类)
///
/// @author scx567888
/// @version 0.0.1
public final class HttpSchemeHelper {

    public static boolean checkIsTLS(ScxURI uri) {
        var scheme = uri.scheme().toLowerCase();
        return switch (scheme) {
            case "http", "ws" -> false;
            case "https", "wss" -> true;
            default -> throw new IllegalArgumentException("Unsupported scheme: " + uri.scheme());
        };
    }

    public static InetSocketAddress getRemoteAddress(ScxURI uri) {
        var host = uri.host();
        var port = uri.port();
        if (port == null) {
            port = getDefaultPort(uri.scheme());
        }
        return new InetSocketAddress(host, port);
    }

    public static int getDefaultPort(String scheme) {
        scheme = scheme.toLowerCase();
        return switch (scheme) {
            case "http", "ws" -> 80;
            case "https", "wss" -> 443;
            default -> throw new IllegalArgumentException("Unsupported scheme: " + scheme);
        };
    }

    /// todo 这个方法怎么处置?
    public static String getRequestTargetStr(ScxURIWritable requestTarget, RequestTargetForm requestTargetForm) {
        return switch (requestTargetForm) {
            case ORIGIN_FORM -> requestTarget.scheme(null).host(null).encode(true);
            case ABSOLUTE_FORM -> {
                // 确保统一小写
                var scheme = requestTarget.scheme().toLowerCase();
                //注意转换 ws -> http
                if ("ws".equals(scheme)) {
                    scheme = "http";
                } else if ("wss".equals(scheme)) {
                    scheme = "https";
                }
                yield requestTarget.scheme(scheme).encode(true);
            }
            case AUTHORITY_FORM -> {
                var port = requestTarget.port();
                if (port == null) {
                    port = getDefaultPort(requestTarget.scheme());
                }
                yield requestTarget.host() + ":" + port;
            }
            case ASTERISK_FORM -> "*";
        };
    }

}
