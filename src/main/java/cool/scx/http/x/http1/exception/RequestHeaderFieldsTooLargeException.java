package cool.scx.http.x.http1.exception;

import dev.scx.http.exception.HttpException;

import static dev.scx.http.status_code.HttpStatusCode.REQUEST_HEADER_FIELDS_TOO_LARGE;

/// RequestHeaderFieldsTooLargeException
///
/// @author scx567888
/// @version 0.0.1
public class RequestHeaderFieldsTooLargeException extends HttpException {

    public RequestHeaderFieldsTooLargeException() {
        super(REQUEST_HEADER_FIELDS_TOO_LARGE);
    }

    public RequestHeaderFieldsTooLargeException(String message) {
        super(REQUEST_HEADER_FIELDS_TOO_LARGE, message);
    }

    public RequestHeaderFieldsTooLargeException(Throwable cause) {
        super(REQUEST_HEADER_FIELDS_TOO_LARGE, cause);
    }

    public RequestHeaderFieldsTooLargeException(String message, Throwable cause) {
        super(REQUEST_HEADER_FIELDS_TOO_LARGE, message, cause);
    }

}
