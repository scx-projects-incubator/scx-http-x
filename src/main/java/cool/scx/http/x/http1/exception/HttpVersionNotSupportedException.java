package cool.scx.http.x.http1.exception;

import dev.scx.http.exception.HttpException;

import static dev.scx.http.status_code.HttpStatusCode.HTTP_VERSION_NOT_SUPPORTED;

/// HttpVersionNotSupportedException
///
/// @author scx567888
/// @version 0.0.1
public class HttpVersionNotSupportedException extends HttpException {

    public HttpVersionNotSupportedException() {
        super(HTTP_VERSION_NOT_SUPPORTED);
    }

    public HttpVersionNotSupportedException(String message) {
        super(HTTP_VERSION_NOT_SUPPORTED, message);
    }

    public HttpVersionNotSupportedException(Throwable cause) {
        super(HTTP_VERSION_NOT_SUPPORTED, cause);
    }

    public HttpVersionNotSupportedException(String message, Throwable cause) {
        super(HTTP_VERSION_NOT_SUPPORTED, message, cause);
    }

}
