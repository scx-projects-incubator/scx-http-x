package cool.scx.http.x.http1.io;

import dev.scx.http.exception.ScxHttpException;
import dev.scx.http.status_code.ScxHttpStatusCode;
import dev.scx.io.exception.ScxIOException;

/// BodyTooLargeException
public class BodyTooLargeException extends ScxIOException implements ScxHttpException {

    private final ScxHttpStatusCode statusCode;

    /// 不允许外界创建
    BodyTooLargeException(ScxHttpStatusCode statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    @Override
    public ScxHttpStatusCode statusCode() {
        return statusCode;
    }

}
