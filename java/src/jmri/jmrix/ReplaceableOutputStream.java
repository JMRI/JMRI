package jmri.jmrix;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An output stream where the stream can be replaced on the fly.
 *
 * @author Daniel Bergqvist (C) 2024
 */
public class ReplaceableOutputStream extends OutputStream {

    private volatile OutputStream _stream;

    public void replaceStream(OutputStream stream) {
        this._stream = stream;
    }

    @Override
    public void write(int b) throws IOException {
        _stream.write(b);
    }

    /** {@inheritDoc} */
    @Override
    public void write(byte b[]) throws IOException {
        _stream.write(b);
    }

    /** {@inheritDoc} */
    @Override
    public void write(byte b[], int off, int len) throws IOException {
        _stream.write(b, off, len);
    }

    /** {@inheritDoc} */
    @Override
    public void flush() throws IOException {
        _stream.flush();
    }

    /** {@inheritDoc} */
    @Override
    public void close() throws IOException {
        _stream.close();
    }

}
