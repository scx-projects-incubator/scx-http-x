package dev.scx.http.x.http1;

import dev.scx.http.body.BodyAlreadyConsumedException;
import dev.scx.http.body.BodyReadException;
import dev.scx.http.body.ScxHttpBody;
import dev.scx.http.headers.ScxHttpHeaders;
import dev.scx.http.media.MediaReader;
import dev.scx.io.ByteInput;
import dev.scx.io.exception.AlreadyClosedException;
import dev.scx.io.exception.ScxIOException;

public record Http1Body(ByteInput byteInput, ScxHttpHeaders headers) implements ScxHttpBody {

    @Override
    public <T> T as(MediaReader<T> mediaReader) throws BodyAlreadyConsumedException, BodyReadException {
        try {
            return mediaReader.read(byteInput, headers);
        } catch (ScxIOException e) {
            throw new BodyReadException(e);
        } catch (AlreadyClosedException e) {
            throw new BodyAlreadyConsumedException();
        }
    }

}
