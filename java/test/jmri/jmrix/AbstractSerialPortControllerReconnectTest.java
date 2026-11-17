package jmri.jmrix;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

import jmri.SystemConnectionMemo;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for reconnecting a serial port after its connection failed: the
 * connection is set up again however many attempts it takes.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class AbstractSerialPortControllerReconnectTest {

    /**
     * A serial port whose connect attempts succeed, fail or throw, in a
     * given order.
     */
    private static class Port extends AbstractSerialPortController {

        static final int OPENS = 0;
        static final int FAILS = 1;
        static final int THROWS = 2;

        final Deque<Integer> attempts = new ArrayDeque<>();
        int connects = 0;
        int resetups = 0;

        Port(Integer... attempts) {
            super(Mockito.mock(SystemConnectionMemo.class));
            this.attempts.addAll(java.util.List.of(attempts));
            allowConnectionRecovery = true;
            reconnectinterval = 0;
        }

        @Override
        public void connect() throws IOException {
            connects++;
            int a = attempts.isEmpty() ? OPENS : attempts.removeFirst();
            if (a == THROWS) {
                throw new IOException("port busy");
            }
            opened = (a == OPENS);
        }

        @Override
        protected void resetupConnection() {
            resetups++;
        }

        @Override
        public String openPort(String portName, String appName) {
            return null;
        }

        @Override
        public void configure() {
        }

        @Override
        public DataInputStream getInputStream() {
            return null;
        }

        @Override
        public DataOutputStream getOutputStream() {
            return null;
        }

        @Override
        public boolean status() {
            return opened;
        }

        @Override
        public String[] validBaudRates() {
            return new String[]{"9600"};
        }

        @Override
        public int[] validBaudNumbers() {
            return new int[]{9600};
        }

        @Override
        public String getCurrentPortName() {
            return "testport";
        }
    }

    @Test
    public void testFirstAttemptOpensAndSetsUpAgain() {
        Port p = new Port(Port.OPENS);
        p.reconnect();
        assertTrue(p.opened);
        assertEquals(1, p.connects);
        assertEquals(1, p.resetups, "the connection is given the reopened port");
    }

    @Test
    public void testLaterAttemptOpensAndSetsUpAgain() {
        Port p = new Port(Port.FAILS, Port.OPENS);
        p.reconnect();
        assertTrue(p.opened);
        assertEquals(2, p.connects);
        assertEquals(1, p.resetups);
    }

    @Test
    public void testFirstAttemptThrowsThenRetried() {
        Port p = new Port(Port.THROWS, Port.OPENS);
        p.reconnect();
        JUnitAppender.assertWarnMessageStartsWith("Error during reconnect");
        assertTrue(p.opened, "an exception on the first attempt doesn't stop the retries");
        assertEquals(1, p.resetups);
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
