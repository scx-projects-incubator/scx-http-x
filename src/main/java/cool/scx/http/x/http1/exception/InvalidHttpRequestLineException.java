package cool.scx.http.x.http1.exception;

import dev.scx.http.exception.BadRequestException;

public class InvalidHttpRequestLineException extends BadRequestException {

    public InvalidHttpRequestLineException(String message) {
        super(message);
    }

}
