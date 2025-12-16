package cool.scx.http.x.http1.body_supplier;

import dev.scx.io.ByteChunk;
import dev.scx.io.ByteInput;
import dev.scx.io.exception.AlreadyClosedException;
import dev.scx.io.exception.ScxIOException;
import dev.scx.io.supplier.ByteSupplier;

public class NullContentByteSupplier implements ByteSupplier {

    private final ByteInput byteInput;

    public NullContentByteSupplier(ByteInput byteInput) {
        this.byteInput = byteInput;
    }

    @Override
    public ByteChunk get() {
        return null;
    }

    @Override
    public void close() throws ScxIOException {
        try {
            byteInput.close();
        } catch (AlreadyClosedException _) {
            // 忽略
        }
    }

}
