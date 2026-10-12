package jmri.jmrix.can.adapters.gridconnect;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests that the GcSerialDriverAdapter input buffer passes an end of stream
 * on to its reader and stops its read thread.
 * <p>
 * jSerialComm reports a removed USB serial port, eg. an unplugged CANUSB4 on
 * Windows, by read() returning -1. The buffer thread used to retry for ever,
 * spinning a core at maximum priority while the reader waited for data that
 * would never come.
 *
 * @author KeithB Copyright (C) 2026
 */
public class GcSerialDriverAdapterEndOfStreamTest {

    private static final String PORT = "EndOfStreamTest";

    private InputStream makeBuffer(InputStream in) throws Exception {
        Class<?> c = Class.forName(GcSerialDriverAdapter.class.getName() + "$AsyncBufferInputStream");
        Constructor<?> ctor = c.getDeclaredConstructor(InputStream.class, String.class);
        ctor.setAccessible(true);
        return (InputStream) ctor.newInstance(in, PORT);
    }

    private static Thread findReadThread() {
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (t.getName().equals("GcSerialPort InputBufferThread " + PORT)) {
                return t;
            }
        }
        return null;
    }

    @Test
    @Timeout(10)
    public void testEndOfStreamIsPassedOn() throws Exception {
        byte[] data = ":SB020N9000020002;".getBytes(StandardCharsets.US_ASCII);
        InputStream buffer = makeBuffer(new ByteArrayInputStream(data));

        // all the data is delivered first
        byte[] got = new byte[data.length];
        int n = 0;
        byte[] one = new byte[1];
        while (n < data.length) {
            int r = buffer.read(one, 0, 1);
            Assertions.assertEquals(1, r, "data byte " + n);
            got[n++] = one[0];
        }
        Assertions.assertArrayEquals(data, got, "data intact");

        // then the end of stream, every time it is asked for
        Assertions.assertEquals(-1, buffer.read(one, 0, 1), "end of stream");
        Assertions.assertEquals(-1, buffer.read(one, 0, 1), "end of stream again");

        // and the read thread has stopped instead of spinning
        JUnitUtil.waitFor(() -> {
            Thread t = findReadThread();
            return t == null || !t.isAlive();
        }, "read thread stopped");

        buffer.close();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
