package cool.scx.http.x.http1.io;

import dev.scx.http.exception.ScxHttpException;
import dev.scx.http.status_code.ScxHttpStatusCode;
import dev.scx.io.exception.ScxIOException;

import static dev.scx.http.status_code.HttpStatusCode.BAD_REQUEST;

/// BodyToShortException
public final class BodyTooShortException extends ScxIOException implements ScxHttpException {

    /// 不允许外界创建
    BodyTooShortException(String message) {
        super(message);
    }

    @Override
    public ScxHttpStatusCode statusCode() {
        return BAD_REQUEST;
    }

}
