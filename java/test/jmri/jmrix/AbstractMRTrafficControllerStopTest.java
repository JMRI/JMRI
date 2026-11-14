package jmri.jmrix;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests that closing a connection while its receive thread is reading is
 * taken as the stop, not reported as a receive error.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class AbstractMRTrafficControllerStopTest {

    private AbstractMRTrafficController tc;
    private AbstractPortControllerScaffold port;

    /**
     * The connection is closed on another thread while the receive loop is
     * reading, and the read then fails: nothing is logged, and no receive
     * error is recorded.
     */
    @Test
    public void testReadFailingAsTheConnectionClosesIsNotAnError() {
        tc.controller = port;
        tc.istream = new DataInputStream(new InputStream() {
            @Override
            public int read() throws IOException {
                tc.disconnectPort(port);
                throw new IOException("Stream closed");
            }
        });
        tc.receiveLoop();
        Assertions.assertFalse(tc.rcvException, "no receive error");
    }

    @BeforeEach
    public void setUp() throws IOException {
        JUnitUtil.setUp();
        port = new AbstractPortControllerScaffold(new InternalSystemConnectionMemo());
        tc = new AbstractMRTrafficController() {
            @Override
            protected void forwardMessage(AbstractMRListener client, AbstractMRMessage m) {
            }

            @Override
            protected AbstractMRMessage pollMessage() {
                return null;
            }

            @Override
            protected AbstractMRListener pollReplyHandler() {
                return null;
            }

            @Override
            protected AbstractMRMessage enterProgMode() {
                return null;
            }

            @Override
            protected AbstractMRMessage enterNormalMode() {
                return null;
            }

            @Override
            protected void forwardReply(AbstractMRListener client, AbstractMRReply m) {
            }

            @Override
            protected AbstractMRReply newReply() {
                return new AbstractMRReply() {
                    @Override
                    protected int skipPrefix(int index) {
                        return index;
                    }
                };
            }

            @Override
            protected boolean endOfMessage(AbstractMRReply r) {
                return true;
            }
        };
    }

    @AfterEach
    public void tearDown() {
        tc.terminateThreads();
        port.dispose();
        JUnitUtil.tearDown();
    }

}
