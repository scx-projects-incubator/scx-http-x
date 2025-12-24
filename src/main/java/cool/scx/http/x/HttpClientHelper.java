package cool.scx.http.x;

import dev.scx.http.uri.ScxURI;
import dev.scx.tcp.tls.TLS;

import javax.net.ssl.SNIHostName;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

/// HttpClientHelper (内部工具类)
///
/// @author scx567888
/// @version 0.0.1
final class HttpClientHelper {

    public static SSLSocket configTLS(Socket tcpSocket, TLS tls, ScxURI uri, String... applicationProtocols) throws IOException {
        SSLSocket sslSocket;
        // 1, 手动升级
        try {
            sslSocket = tls.upgradeToTLS(tcpSocket);
        } catch (IOException e) {
            try {
                tcpSocket.close();
            } catch (IOException ex) {
                e.addSuppressed(ex);
            }
            throw new IOException("升级到 TLS 时发生错误 !!!", e);
        }

        // 2, 配置 参数
        sslSocket.setUseClientMode(true);

        var sslParameters = sslSocket.getSSLParameters();

        sslParameters.setApplicationProtocols(applicationProtocols);
        sslParameters.setServerNames(List.of(new SNIHostName(uri.host())));

        // 别忘了写回 参数
        sslSocket.setSSLParameters(sslParameters);

        // 3, 开始握手
        try {
            sslSocket.startHandshake();
        } catch (IOException e) {
            try {
                tcpSocket.close();
            } catch (IOException ex) {
                e.addSuppressed(ex);
            }
            throw new IOException("处理 TLS 握手 时发生错误 !!!", e);
        }

        return sslSocket;
    }

    public static boolean checkIsTLS(String scheme) {
        scheme = scheme.toLowerCase();
        return switch (scheme) {
            case "http", "ws" -> false;
            case "https", "wss" -> true;
            default -> throw new IllegalArgumentException("Unsupported scheme: " + scheme);
        };
    }

    public static int getDefaultPort(String scheme) {
        scheme = scheme.toLowerCase();
        return switch (scheme) {
            case "http", "ws" -> 80;
            case "https", "wss" -> 443;
            default -> throw new IllegalArgumentException("Unsupported scheme: " + scheme);
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

//    /// todo 这个方法怎么处置?
//    public static String getRequestTargetStr(ScxURIWritable requestTarget, RequestTargetForm requestTargetForm) {
//        return switch (requestTargetForm) {
//            case ORIGIN_FORM -> requestTarget.scheme(null).host(null).encode(true);
//            case ABSOLUTE_FORM -> {
//                // 确保统一小写
//                var scheme = requestTarget.scheme().toLowerCase();
//                //注意转换 ws -> http
//                if ("ws".equals(scheme)) {
//                    scheme = "http";
//                } else if ("wss".equals(scheme)) {
//                    scheme = "https";
//                }
//                yield requestTarget.scheme(scheme).encode(true);
//            }
//            case AUTHORITY_FORM -> {
//                var port = requestTarget.port();
//                if (port == null) {
//                    port = getDefaultPort(requestTarget.scheme());
//                }
//                yield requestTarget.host() + ":" + port;
//            }
//            case ASTERISK_FORM -> "*";
//        };
//    }

}
