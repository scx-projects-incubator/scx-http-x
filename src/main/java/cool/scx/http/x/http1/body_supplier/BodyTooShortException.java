package cool.scx.http.x.http1.body_supplier;

import dev.scx.http.exception.ScxHttpException;
import dev.scx.http.status_code.ScxHttpStatusCode;
import dev.scx.io.exception.ScxIOException;

/// BodyToShortException
public class BodyTooShortException extends ScxIOException implements ScxHttpException {

    private final ScxHttpStatusCode statusCode;

    /// 不允许外界创建
    BodyTooShortException(ScxHttpStatusCode statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    @Override
    public ScxHttpStatusCode statusCode() {
        return statusCode;
    }

}
