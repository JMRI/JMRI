package jmri.jmrix.nce;

import java.io.*;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * JUnit tests for the NceTrafficController class
 *
 * @author Bob Jacobsen Copyright 2003, 2007
 */
public class NceTrafficControllerTest extends jmri.jmrix.AbstractMRTrafficControllerTest {

    @Test
    @Disabled("Test disabled until threading can be resolved")
    public void testSendAscii() throws IOException {
        NceTrafficController c = new NceTrafficController() {
            // skip timeout message
            @Override
            protected void handleTimeout(jmri.jmrix.AbstractMRMessage msg, jmri.jmrix.AbstractMRListener l) {
            }

            @Override
            public void receiveLoop() {
            }

            @Override
            protected void portWarn(Exception e) {
            }
        };

        // connect to iostream via port controller
        NcePortControllerScaffold p = new NcePortControllerScaffold();
        c.connectPort(p);

        // send a message
        NceMessage m = new NceMessage(3);
        m.setBinary(false);
        m.setOpCode('0');
        m.setElement(1, '1');
        m.setElement(2, '2');
        c.sendNceMessage(m, new NceListenerScaffold());
        assertEquals( 4, tostream.available(), "total length ");
        assertEquals( (byte)'0', tostream.readByte(), "Char 0");
        assertEquals( (byte)'1', tostream.readByte(), "Char 1");
        assertEquals( (byte)'2', tostream.readByte(), "Char 2");
        assertEquals( 0x0d, tostream.readByte(), "EOM");
        assertEquals( 0, tostream.available(), "remaining ");
    }

    @Test
    @Disabled("Test disabled until threading can be resolved")
    public void testSendBinary() throws IOException {
        NceTrafficController c = new NceTrafficController() {
            // skip timeout message
            @Override
            protected void handleTimeout(jmri.jmrix.AbstractMRMessage msg, jmri.jmrix.AbstractMRListener l) {
            }

            @Override
            public void receiveLoop() {
            }

            @Override
            protected void portWarn(Exception e) {
            }
        };

        // connect to iostream via port controller
        NcePortControllerScaffold p = new NcePortControllerScaffold();
        c.connectPort(p);

        // send a message
        NceMessage m = new NceMessage(3);
        m.setBinary(true);
        m.setOpCode(0x81);
        m.setElement(1, 0x12);
        m.setElement(2, 0x34);
        c.sendNceMessage(m, new NceListenerScaffold());
        assertEquals( 3, tostream.available(), "total length ");
        assertEquals( 0x81, 0xFF & tostream.readByte(), "Char 0");
        assertEquals( 0x12, tostream.readByte(), "Char 1");
        assertEquals( 0x34, tostream.readByte(), "Char 2");
        assertEquals( 0, tostream.available(), "remaining ");
    }

    @Test
    @Disabled("Test disabled until threading can be resolved")
    public void testMonitor() throws IOException {
        NceTrafficController c = new NceTrafficController() {
            // skip timeout message
            @Override
            protected void handleTimeout(jmri.jmrix.AbstractMRMessage msg, jmri.jmrix.AbstractMRListener l) {
            }

            @Override
            public void receiveLoop() {
            }

            @Override
            protected void portWarn(Exception e) {
            }
        };

        // connect to iostream via port controller
        NcePortControllerScaffold p = new NcePortControllerScaffold();
        c.connectPort(p);

        // start monitor
        NceListenerScaffold s = new NceListenerScaffold();
        c.addNceListener(s);

        // send a message
        NceMessage m = new NceMessage(3);
        m.setOpCode('0');
        m.setElement(1, '1');
        m.setElement(2, '2');
        c.sendNceMessage(m, new NceListenerScaffold());

        // check it arrived at monitor
        assertEquals( 4, tostream.available(), "total length ");
        assertEquals( (byte)'0', tostream.readByte(), "Char 0");
        assertEquals( (byte)'1', tostream.readByte(), "Char 1");
        assertEquals( (byte)'2', tostream.readByte(), "Char 2");
        assertEquals( 0x0d, tostream.readByte(), "EOM");
        assertEquals( 0, tostream.available(), "remaining");
    }

    @Test
    @Disabled("Test disabled until threading can be resolved")
    public void xtestRcvReply() throws IOException {
        NceTrafficController c = new NceTrafficController() {
            // skip timeout message
            @Override
            protected void handleTimeout(jmri.jmrix.AbstractMRMessage msg, jmri.jmrix.AbstractMRListener l) {
            }

            @Override
            public void receiveLoop() {
            }

            @Override
            protected void portWarn(Exception e) {
            }
        };

        // connect to iostream via port controller
        NcePortControllerScaffold p = new NcePortControllerScaffold();
        c.connectPort(p);

        // object to receive reply
        NceListenerScaffold l = new NceListenerScaffold();
        c.addNceListener(l);

        // send a message
        NceMessage m = new NceMessage(3);
        m.setOpCode('0');
        m.setElement(1, '1');
        m.setElement(2, '2');
        c.sendNceMessage(m, l);
        // that's already tested, so don't do here.

        assertEquals( m, l.rcvdMsg);

        // now send reply
        tistream.write('R');
        tistream.write(0x0d);
        tistream.write('C');
        tistream.write('O');
        tistream.write('M');
        tistream.write('M');
        tistream.write('A');
        tistream.write('N');
        tistream.write('D');
        tistream.write(':');
        tistream.write(' ');

        // drive the mechanism
        c.handleOneIncomingReply();
        JUnitUtil.waitFor( () -> { return l.rcvdReply != null; }, "Reply received");
        assertEquals( 'R', l.rcvdReply.getOpCode(), "first char of reply ");
    }

    // internal class to simulate a NcePortController
    private class NcePortControllerScaffold extends NcePortController {

        protected NcePortControllerScaffold() {
            super(null);
            assertDoesNotThrow( () -> {
                PipedInputStream tempPipe;
                tempPipe = new PipedInputStream();
                tostream = new DataInputStream(tempPipe);
                ostream = new DataOutputStream(new PipedOutputStream(tempPipe));
                tempPipe = new PipedInputStream();
                istream = new DataInputStream(tempPipe);
                tistream = new DataOutputStream(new PipedOutputStream(tempPipe));
            });
        }

        @Override
        public java.util.Vector<String> getPortNames() {
            return null;
        }

        @Override
        public String openPort(String portName, String appName) {
            return null;
        }

        @Override
        public void configure() {
        }

        @Override
        public String[] validBaudRates() {
            return new String[]{};
        }

        //@Override
        @Override
        public int[] validBaudNumbers() {
            return new int[]{};
        }

        // returns the InputStream from the port
        @Override
        public DataInputStream getInputStream() {
            return istream;
        }

        // returns the outputStream to the port
        @Override
        public DataOutputStream getOutputStream() {
            return ostream;
        }

        // check that this object is ready to operate
        @Override
        public boolean status() {
            return true;
        }
    }

    private DataOutputStream ostream;  // Traffic controller writes to this
    private DataInputStream tostream; // so we can read it from this

    private DataOutputStream tistream; // tests write to this
    private DataInputStream istream;  // so the traffic controller can read from this

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        tc = new NceTrafficController();
    }

    @Override
    @AfterEach
    public void tearDown() {
        tc.terminateThreads();
        tc = null;
        JUnitUtil.tearDown();
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NceTrafficControllerTest.class);

}
