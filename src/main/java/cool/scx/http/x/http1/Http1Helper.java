package cool.scx.http.x.http1;

import cool.scx.http.x.http1.headers.Http1Headers;
import cool.scx.http.x.http1.headers.upgrade.ScxUpgrade;
import cool.scx.http.x.http1.request_line.Http1RequestLine;
import cool.scx.http.x.http1.request_line.request_target.*;
import dev.scx.http.exception.BadRequestException;
import dev.scx.http.headers.ScxHttpHeadersWritable;
import dev.scx.http.method.ScxHttpMethod;
import dev.scx.http.peer_info.PeerInfo;
import dev.scx.http.peer_info.PeerInfoWritable;
import dev.scx.http.status_code.ScxHttpStatusCode;
import dev.scx.http.uri.ScxURI;
import dev.scx.io.ByteInput;
import dev.scx.io.ByteOutput;
import dev.scx.io.exception.AlreadyClosedException;
import dev.scx.io.exception.ScxIOException;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static cool.scx.http.x.http1.headers.connection.Connection.UPGRADE;
import static dev.scx.http.headers.HttpHeaderName.HOST;
import static dev.scx.http.method.HttpMethod.*;
import static dev.scx.http.status_code.HttpStatusCode.*;

/// Http1Helper
///
/// @author scx567888
/// @version 0.0.1
public final class Http1Helper {

    public static final byte[] CONTINUE_100_BYTES = "HTTP/1.1 100 Continue\r\n\r\n".getBytes(StandardCharsets.UTF_8);
    public static final byte[] CRLF_BYTES = "\r\n".getBytes();
    public static final byte[] CRLF_CRLF_BYTES = "\r\n\r\n".getBytes();

    /// 检查是不是 升级请求 如果不是 返回 null
    public static ScxUpgrade checkUpgradeRequest(Http1RequestLine requestLine, Http1Headers headers) {
        return requestLine.method() == GET && headers.connection() == UPGRADE ? headers.upgrade() : null;
    }

    /// 发送 CONTINUE_100
    public static void sendContinue100(ByteOutput byteOutput) throws ScxIOException, AlreadyClosedException {
        byteOutput.write(CONTINUE_100_BYTES);
    }

    /// 消耗 Body
    public static void consumeBodyByteInput(ByteInput bodyByteInput) {
        // 因为我们的 Body 流 在 close 时会自动排空, 这里直接 close 即可
        try {
            bodyByteInput.close();
        } catch (AlreadyClosedException | ScxIOException e) {
            // 忽略异常
        }
    }

    /// 验证 Http/1.1 中的 Host, 这里我们只校验是否存在且只有一个值
    public static void validateHost(ScxHttpHeadersWritable headers) throws BadRequestException {
        var allHost = headers.getAll(HOST);
        int size = allHost.size();
        if (size == 0) {
            throw new BadRequestException("HOST header is empty");
        }
        if (size > 1) {
            throw new BadRequestException("HOST header contains more than one value");
        }
        var hostValue = allHost.get(0);
        if (hostValue.isBlank()) {
            throw new BadRequestException("HOST header is empty");
        }
    }

    /// 检查响应是否 存在响应体
    public static boolean checkResponseHasBody(ScxHttpStatusCode status) {
        return SWITCHING_PROTOCOLS != status &&
            NO_CONTENT != status &&
            NOT_MODIFIED != status;
    }

    /// 检查请求是否 存在请求体
    public static boolean checkRequestHasBody(ScxHttpMethod method) {
        return GET != method;
    }

    public static PeerInfoWritable getRemotePeer(Socket tcpSocket) {
        var address = (InetSocketAddress) tcpSocket.getRemoteSocketAddress();
        //todo 未完成 tls 信息没有写入
        return PeerInfo.of().address(address).host(address.getHostString()).port(address.getPort());
    }

    public static PeerInfoWritable getLocalPeer(Socket tcpSocket) {
        var address = (InetSocketAddress) tcpSocket.getLocalSocketAddress();
        //todo 未完成 tls 信息没有写入
        return PeerInfo.of().address(address).host(address.getHostString()).port(address.getPort());
    }

    /// 创建 RequestTarget
    public static RequestTarget createRequestTarget(ScxHttpMethod method, ScxURI uri, boolean useProxy) {
        var scheme = uri.scheme();
        var host = uri.host();
        var port = uri.port();
        var path = uri.path();
        var query = uri.query();
        var fragment = uri.fragment();
        if (method == CONNECT) {
            return new AuthorityForm(host, port);
        } else if (method == OPTIONS) {
            // 如果 uri 所有组件都是 null 就表示 是 AsteriskForm
            if (scheme == null && host == null && port == null && path == null && query.isEmpty() && fragment == null) {
                return AsteriskForm.of();
            }
        }
        if (useProxy) {
            return new AbsoluteForm(scheme, host, port, path, query, fragment);
        } else {
            // OriginForm 必须 / 起始, 我们在此处 处理 null 和 "" -> "/" 的兼容
            var finalPath = path == null || path.isEmpty() ? "/" : path;
            return new OriginForm(finalPath, query, fragment);
        }
    }

}
