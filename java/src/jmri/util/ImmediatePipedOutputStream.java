package jmri.util;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * Makes a workaround for standard {@link PipedOutputStream} wait.
 * <p>The {@link PipedInputStream#read()}, in case the receive buffer is
 * empty at the time of the call, waits for up to 1000ms.
 * {@link PipedOutputStream#write(int)} does call <code>sink.receive</code>,
 * but does not <code>notify()</code> the sink object so that read's
 * wait() terminates.
 * <p>
 * As a result, the read side of the pipe waits full 1000ms even though data
 * become available during the wait.
 * <p>
 * The workaround is to simply {@link PipedOutputStream#flush} after write,
 * which returns from wait()s immediately.
 *
 * @author Svata Dedic Copyright (C) 2020
 */

public class ImmediatePipedOutputStream extends PipedOutputStream {
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        super.write(b, off, len);
        flush();
    }

    @Override
    public void write(int b) throws IOException {
        super.write(b);
        flush();
    }
}
