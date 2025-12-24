package cool.scx.http.x.http1.request_line;

import dev.scx.http.exception.ScxHttpException;
import dev.scx.http.status_code.ScxHttpStatusCode;

import static dev.scx.http.status_code.HttpStatusCode.BAD_REQUEST;

public final class InvalidRequestLineException extends Exception implements ScxHttpException {

    public final String requestLineStr;

    public InvalidRequestLineException(String requestLineStr) {
        super("Invalid RequestLine : " + requestLineStr);
        this.requestLineStr = requestLineStr;
    }

    @Override
    public ScxHttpStatusCode statusCode() {
        return BAD_REQUEST;
    }

}
