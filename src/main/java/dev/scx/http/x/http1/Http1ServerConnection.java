package dev.scx.http.x.http1;

import dev.scx.http.x.http1.headers.Http1Headers;
import cool.scx.http.x.http1.io.*;
import dev.scx.http.x.http1.io.*;
import dev.scx.http.x.http1.request_line.Http1RequestLine;
import dev.scx.http.x.http1.request_line.InvalidRequestLineException;
import dev.scx.http.x.http1.request_line.InvalidRequestLineHttpVersionException;
import dev.scx.http.x.http1.request_line.request_target.OriginForm;
import dev.scx.function.Function1Void;
import dev.scx.http.ScxHttpServerRequest;
import dev.scx.http.error_handler.ErrorPhase;
import dev.scx.http.error_handler.ScxHttpServerErrorHandler;
import dev.scx.http.method.ScxHttpMethod;
import dev.scx.io.ByteInput;
import dev.scx.io.ByteOutput;
import dev.scx.io.ScxIO;
import dev.scx.io.exception.AlreadyClosedException;
import dev.scx.io.exception.NoMoreDataException;
import dev.scx.io.exception.ScxIOException;
import dev.scx.io.input.NullByteInput;
import dev.scx.io.supplier.ByteSupplier;

import java.io.IOException;
import java.lang.System.Logger;
import java.net.Socket;

import static dev.scx.http.x.http1.Http1Helper.*;
import static dev.scx.http.x.http1.headers.connection.Connection.CLOSE;
import static dev.scx.http.x.http1.headers.expect.Expect.CONTINUE;
import static dev.scx.http.error_handler.DefaultHttpServerErrorHandler.DEFAULT_HTTP_SERVER_ERROR_HANDLER;
import static dev.scx.http.error_handler.ErrorHandlerHelper.getErrorPhaseString;
import static dev.scx.http.error_handler.ErrorPhase.SYSTEM;
import static dev.scx.http.error_handler.ErrorPhase.USER;
import static dev.scx.io.ScxIO.createByteInput;
import static dev.scx.io.supplier.ClosePolicyByteSupplier.noCloseDrain;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.getLogger;


/// Http 1.1 连接处理器
///
/// @author scx567888
/// @version 0.0.1
public class Http1ServerConnection {

    private final static Logger LOGGER = getLogger(Http1ServerConnection.class.getName());

    public final Socket tcpSocket;
    public final ByteInput dataReader;
    public final ByteOutput dataWriter;

    private final Http1ServerConnectionOptions options;
    private final Function1Void<ScxHttpServerRequest, ?> requestHandler;
    private final ScxHttpServerErrorHandler errorHandler;
    private boolean running;

    public Http1ServerConnection(Socket tcpSocket, Http1ServerConnectionOptions options, Function1Void<ScxHttpServerRequest, ?> requestHandler, ScxHttpServerErrorHandler errorHandler) throws IOException {
        this.tcpSocket = tcpSocket;
        this.dataReader = createByteInput(this.tcpSocket.getInputStream());
        this.dataWriter = ScxIO.createByteOutput(this.tcpSocket.getOutputStream());
        this.options = options;
        this.requestHandler = requestHandler;
        this.errorHandler = errorHandler;
        this.running = true;
    }

    public void start() {

        //开始读取 Http 请求
        while (running) {

            //1, 我们先读取请求
            ScxHttpServerRequest request;
            try {
                request = readRequest();
            } catch (CloseConnectionException e) {
                // 底层连接已断开 我们停止解析即可
                stop();
                break;
            } catch (Throwable e) {
                // 如果读取请求失败 我们将其理解为系统错误 不可恢复
                handlerSystemException(e);
                break;
            }

            //2, 交由用户处理器处理
            try {
                _callRequestHandler(request);
            } catch (Throwable e) {
                //用户处理器 错误 我们尝试恢复
                handlerUserException(e, request);
            } finally {
                //todo 这里如果  _callRequestHandler 中 异步读取 body 怎么办?

                // 6, 如果 还是 running 说明需要继续复用当前 tcp 连接,并进行下一次 Request 的读取
                if (running) {
                    // 7, 用户处理器可能没有消费完请求体 这里我们帮助消费用户未消费的数据
                    consumeBodyByteInput(request.body().byteInput());
                }
            }

        }

    }

    private ScxHttpServerRequest readRequest() throws CloseConnectionException, InvalidRequestLineException, InvalidRequestLineHttpVersionException, RequestLineTooLongException, HeaderTooLargeException, ContentLengthBodyTooLargeException {
        // 1, 读取 请求行
        Http1RequestLine requestLine;
        try {
            requestLine = Http1Reader.readRequestLine(dataReader, options.maxRequestLineSize());
        } catch (ScxIOException | AlreadyClosedException | NoMoreDataException e) {
            // 这表示底层 socket 出现问题 我们需要关闭连接
            throw new CloseConnectionException();
        } catch (RequestLineTooLongException | InvalidRequestLineException | InvalidRequestLineHttpVersionException e) {
            // 其余异常我们可以 向客户端返回
            throw e;
        }

        // 2, 读取 请求头
        Http1Headers headers;
        try {
            headers = Http1Reader.readHeaders(dataReader, options.maxHeaderSize());
        } catch (ScxIOException | AlreadyClosedException | NoMoreDataException e) {
            // 这表示底层 socket 出现问题 我们需要关闭连接
            throw new CloseConnectionException();
        } catch (HeaderTooLargeException e) {
            // 其余异常我们可以 向客户端返回
            throw e;
        }

        // 3, 读取 请求体流
        ByteSupplier bodyByteSupplier;
        try {
            bodyByteSupplier = Http1Reader.readBodyByteInput(headers, dataReader, options.maxPayloadSize());
        } catch (ContentLengthBodyTooLargeException e) {
            // 其余异常我们可以 向客户端返回
            throw e;
        }

        // 4, 在交给用户处理器进行处理之前, 我们需要做一些预处理

        // 4.1, 验证 请求头
        if (options.validateHost()) {
            validateHost(headers);
        }

        // 4.2, 处理 100-continue 临时请求
        if (headers.expect() == CONTINUE) {
            // 如果启用了自动响应 我们直接发送
            if (options.autoRespond100Continue()) {
                try {
                    sendContinue100(dataWriter);
                } catch (ScxIOException | AlreadyClosedException e) {
                    // 如果发生错误终止 连接
                    throw new CloseConnectionException("Failed to write continue", e);
                }
            } else {
                // 否则交给用户去处理
                bodyByteSupplier = new AutoContinueByteSupplier(bodyByteSupplier, dataWriter);
            }
        }

        // 创建一个 ByteInput, 要求是 1, 要隔离 底层 close, 2, 同时在 close 的时候还要排空流.
        var bodyByteInput = createByteInput(noCloseDrain(bodyByteSupplier));

        // 5, 判断是否为 升级请求 并创建对应请求
        var upgrade = checkUpgradeRequest(requestLine, headers);

        if (upgrade != null) {
            for (var upgradeHandler : options.upgradeHandlerList()) {
                var canHandle = upgradeHandler.canUpgrade(upgrade);
                if (canHandle) {
                    return upgradeHandler.createUpgradedRequest(this, requestLine, headers, bodyByteInput);
                }
            }
        }

        // 否则创建普通请求
        return new Http1ServerRequest(this, requestLine, headers, bodyByteInput);
    }

    public void stop() {
        //停止读取 http 请求
        running = false;
    }

    public void close() throws IOException {
        //关闭连接就表示 不需要继续读取了
        stop();
        //关闭连接
        tcpSocket.close();
    }

    public boolean isRunning() {
        return running;
    }

    private void _callRequestHandler(ScxHttpServerRequest request) throws Throwable {
        if (requestHandler != null) {
            requestHandler.apply(request);
        }
    }

    /// 处理系统级别错误
    private void handlerSystemException(Throwable e) {
        //此时我们并没有拿到一个完整的 request 对象 所以这里创建一个 虚拟 request 用于后续响应
        var fakeRequest = new Http1ServerRequest(
            this,
            new Http1RequestLine(ScxHttpMethod.of("unknown"), new OriginForm(null, null, null)),
            new Http1Headers().connection(CLOSE),
            new NullByteInput()
        );
        handlerException(e, fakeRequest, SYSTEM);

        // 系统错误 表示当前连接不合法, 这里我们停止监听并关闭连接
        try {
            close();
        } catch (IOException _) {

        }
    }

    /// 处理用户级别错误
    private void handlerUserException(Throwable e, ScxHttpServerRequest request) {
        handlerException(e, request, USER);
    }

    private void handlerException(Throwable e, ScxHttpServerRequest request, ErrorPhase errorPhase) {

        if (tcpSocket.isClosed()) {
            LOGGER.log(ERROR, getErrorPhaseString(errorPhase) + " 发生异常 !!!, 因为 Socket 已被关闭, 所以错误信息可能没有正确返回给客户端 !!!", e);
            return;
        }

        // todo 这里有问题 是不是应该 关闭 连接?
        switch (request.response().senderStatus()) {
            case SUCCESS -> {
                // 这里表示 响应对象已经正确响应了 我们只能打印日志
                LOGGER.log(ERROR, getErrorPhaseString(errorPhase) + " 发生异常 !!!, 因为请求已被相应, 所以错误信息可能没有正确返回给客户端 !!!", e);
                return;
            }
            case SENDING -> {
                // 这里表示 响应对象已经被部分发送了 我们只能打印日志
                LOGGER.log(ERROR, getErrorPhaseString(errorPhase) + " 发生异常 !!!, 因为请求已被部分相应, 所以错误信息可能没有正确返回给客户端 !!!", e);
                return;
            }
            case FAILED -> {
                //这里表示 响应对象已经被部分发送失败使用了 我们只能打印日志
                LOGGER.log(ERROR, getErrorPhaseString(errorPhase) + " 发生异常 !!!, 因为请求被部分相应失败, 所以错误信息可能没有正确返回给客户端 !!!", e);
                return;
            }
        }

        try {
            if (errorHandler != null) {
                errorHandler.accept(e, request, errorPhase);
            } else {
                //没有就回退到默认
                DEFAULT_HTTP_SERVER_ERROR_HANDLER.accept(e, request, errorPhase);
            }
        } catch (Exception ex) {
            e.addSuppressed(ex);
            LOGGER.log(ERROR, getErrorPhaseString(errorPhase) + " 发生异常 !!!, 尝试通过 错误处理器 响应给客户端时发生异常 !!!", e);
        }

    }

}
