package jmri.jmrix;

import java.io.*;

/**
 * An input stream where the stream can be replaced on the fly.
 *
 * @author Daniel Bergqvist (C) 2024
 */
public class ReplaceableInputStream extends InputStream {

    private volatile InputStream _stream;

    public void replaceStream(InputStream stream) {
        this._stream = stream;
    }

    /** {@inheritDoc} */
    @Override
    public int read() throws IOException {
        return _stream.read();
    }

    /** {@inheritDoc} */
    @Override
    public int read(byte b[]) throws IOException {
        return _stream.read(b);
    }

    /** {@inheritDoc} */
    @Override
    public int read(byte b[], int off, int len) throws IOException {
        return _stream.read(b, off, len);
    }

    /** {@inheritDoc} */
    @Override
    public byte[] readAllBytes() throws IOException {
        return _stream.readAllBytes();
    }

    /** {@inheritDoc} */
    @Override
    public byte[] readNBytes(int len) throws IOException {
        return _stream.readNBytes(len);
    }

    /** {@inheritDoc} */
    @Override
    public int readNBytes(byte[] b, int off, int len) throws IOException {
        return _stream.readNBytes(b, off, len);
    }

    /** {@inheritDoc} */
    @Override
    public long skip(long n) throws IOException {
        return _stream.skip(n);
    }

    /** {@inheritDoc} */
    @Override
    public int available() throws IOException {
        return _stream.available();
    }

    /** {@inheritDoc} */
    @Override
    public void close() throws IOException {
        _stream.close();
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void mark(int readlimit) {
        _stream.mark(readlimit);
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void reset() throws IOException {
        _stream.reset();
    }

    /** {@inheritDoc} */
    @Override
    public boolean markSupported() {
        return _stream.markSupported();
    }

    /** {@inheritDoc} */
    @Override
    public long transferTo(OutputStream out) throws IOException {
        return _stream.transferTo(out);
    }

}
