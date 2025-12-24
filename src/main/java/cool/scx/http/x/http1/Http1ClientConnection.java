package cool.scx.http.x.http1;

import cool.scx.http.x.http1.headers.Http1Headers;
import cool.scx.http.x.http1.io.ContentLengthBodyTooLargeException;
import cool.scx.http.x.http1.io.HeaderTooLargeException;
import cool.scx.http.x.http1.io.Http1Reader;
import cool.scx.http.x.http1.io.StatusLineToLongException;
import cool.scx.http.x.http1.status_line.Http1StatusLine;
import cool.scx.http.x.http1.status_line.InvalidStatusLineException;
import cool.scx.http.x.http1.status_line.InvalidStatusLineHttpVersionException;
import cool.scx.http.x.http1.status_line.InvalidStatusLineStatusCodeException;
import dev.scx.http.headers.ScxHttpHeaders;
import dev.scx.http.media.MediaWriter;
import dev.scx.io.ByteInput;
import dev.scx.io.ByteOutput;
import dev.scx.io.ScxIO;
import dev.scx.io.exception.AlreadyClosedException;
import dev.scx.io.exception.NoMoreDataException;
import dev.scx.io.exception.ScxIOException;
import dev.scx.io.supplier.ByteSupplier;

import java.io.IOException;
import java.net.Socket;

import static cool.scx.http.x.http1.io.Http1Writer.sendRequestHeaders;
import static dev.scx.io.ScxIO.createByteInput;
import static dev.scx.io.supplier.ClosePolicyByteSupplier.noCloseDrain;

/// Http1ClientConnection
///
/// @author scx567888
/// @version 0.0.1
public class Http1ClientConnection {

    public final Socket tcpSocket;
    public final ByteInput dataReader;
    public final ByteOutput dataWriter;

    private final Http1ClientConnectionOptions options;

    public Http1ClientConnection(Socket tcpSocket, Http1ClientConnectionOptions options) throws IOException {
        this.tcpSocket = tcpSocket;
        this.dataReader = createByteInput(tcpSocket.getInputStream());
        this.dataWriter = ScxIO.createByteOutput(tcpSocket.getOutputStream());
        this.options = options;
    }

    public Http1ClientConnection sendRequest(Http1ClientRequest request, MediaWriter writer) throws IOException {
        // 复制一份头
        var tempHeaders = new Http1Headers(request.headers());

        // 处理 headers 以及获取 请求长度
        var expectedLength = writer.beforeWrite(tempHeaders, ScxHttpHeaders.of());

        // 发送头
        var byteOutput = sendRequestHeaders(expectedLength, request, this, tempHeaders);

        // 调用处理器
        writer.write(byteOutput);

        return this;
    }

    // 这里的异常需要精细化处理
    public Http1ClientResponse waitResponse() {
        //1, 读取状态行
        Http1StatusLine statusLine;
        try {
            statusLine = Http1Reader.readStatusLine(dataReader, options.maxStatusLineSize());
        } catch (ScxIOException | AlreadyClosedException | NoMoreDataException e) {
            // 底层异常 直接抛出
            throw e;
        } catch (StatusLineToLongException | InvalidStatusLineException | InvalidStatusLineStatusCodeException |
                 InvalidStatusLineHttpVersionException e) {
            // 包装成  RuntimeException todo 此处存疑.
            throw new RuntimeException(e);
        }

        //2, 读取响应头
        Http1Headers headers;
        try {
            headers = Http1Reader.readHeaders(dataReader, options.maxHeaderSize());
        } catch (ScxIOException | AlreadyClosedException | NoMoreDataException e) {
            // 底层异常 直接抛出
            throw e;
        } catch (HeaderTooLargeException e) {
            // 包装成  RuntimeException todo 此处存疑.
            throw new RuntimeException(e);
        }

        //3, 读取响应体
        ByteSupplier bodyByteSupplier;
        try {
            bodyByteSupplier = Http1Reader.readBodyByteInput(headers, dataReader, options.maxPayloadSize());
        } catch (ContentLengthBodyTooLargeException e) {
            // 包装成  RuntimeException todo 此处存疑.
            throw new RuntimeException(e);
        }

        //4, 创建一个 ByteInput, 要求是 1, 要隔离 底层 close, 2, 同时在 close 的时候还要排空流.
        var bodyByteInput = createByteInput(noCloseDrain(bodyByteSupplier));

        return new Http1ClientResponse(statusLine, headers, bodyByteInput);
    }

}
