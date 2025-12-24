package cool.scx.http.x.http1.io;

import dev.scx.http.exception.ScxHttpException;
import dev.scx.http.status_code.HttpStatusCode;
import dev.scx.http.status_code.ScxHttpStatusCode;

public final class BodyToLargeException extends Exception implements ScxHttpException {

    public BodyToLargeException(String message) {
        super(message);
    }

    @Override
    public ScxHttpStatusCode statusCode() {
        return HttpStatusCode.CONTENT_TOO_LARGE;
    }

}
