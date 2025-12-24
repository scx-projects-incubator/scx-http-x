package cool.scx.http.x.http1.io;

import dev.scx.http.exception.ScxHttpException;
import dev.scx.http.status_code.HttpStatusCode;
import dev.scx.http.status_code.ScxHttpStatusCode;

public final class HeaderTooLargeException extends Exception implements ScxHttpException {

    HeaderTooLargeException(String message) {
        super(message);
    }

    @Override
    public ScxHttpStatusCode statusCode() {
        return HttpStatusCode.REQUEST_HEADER_FIELDS_TOO_LARGE;
    }

}
