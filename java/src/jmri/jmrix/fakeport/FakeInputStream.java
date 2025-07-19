package jmri.jmrix.fakeport;

import java.io.IOException;
import java.io.InputStream;

/**
 * A fake input stream.
 *
 * @author Daniel Bergqvist (C) 2024
 */
public class FakeInputStream extends InputStream {

    @Override
    public int read() throws IOException {
        try {
            // This stream will never receive anything. Wait some time
            // to minimize CPU.
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            // Do nothing. This method will return -1 anyway.
        }
        return -1;
    }

}
