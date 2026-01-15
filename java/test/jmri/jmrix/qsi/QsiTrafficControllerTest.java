package jmri.jmrix.qsi;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.*;

import java.util.Vector;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * JUnit tests for the QsiTrafficController class.
 *
 * @author Bob Jacobsen
 */
public class QsiTrafficControllerTest {

    @Test
    public void testCreate() {
        QsiTrafficController m = new QsiTrafficController();
        assertNotNull( m, "exists");
    }

    @Test
    public void testSendAscii() throws IOException {
        QsiTrafficController c = new QsiTrafficController() {
            // skip timeout message
            //protected void handleTimeout(jmri.jmrix.AbstractMRMessage msg,jmri.jmrix.AbstractMRListener l) {};
            //public void receiveLoop() {}
            //protected void portWarn(Exception e) {}
        };

        // connect to iostream via port controller
        QsiPortControllerScaffold p = new QsiPortControllerScaffold();
        c.connectPort(p);

        // send a message
        QsiMessage m = new QsiMessage(3);
        m.setElement(0, 11);
        m.setElement(1, 0);
        m.setElement(2, 0);
        c.sendQsiMessage(m, new QsiListenerScaffold());
        JUnitUtil.waitFor(2); // relinquish control

        assertEquals( 6, tostream.available(), "total length ");
        assertEquals( (byte)'S', tostream.readByte(), "Lead S");
        assertEquals( 11, tostream.readByte(), "Byte 0");
        assertEquals( 0, tostream.readByte(), "Byte 1");
        assertEquals( 0, tostream.readByte(), "Byte 2");
        assertEquals( 11, tostream.readByte(), "Check");
        assertEquals( (byte)'E', tostream.readByte(), "E");
        assertEquals( 0, tostream.available(), "remaining");
    }

    @Test
    public void testMonitor() throws IOException {
        QsiTrafficController c = new QsiTrafficController() {
            // skip timeout message
            //protected void handleTimeout(jmri.jmrix.AbstractMRMessage msg,jmri.jmrix.AbstractMRListener l) {};
            //public void receiveLoop() {}
            //protected void portWarn(Exception e) {}
        };

        // connect to iostream via port controller
        QsiPortControllerScaffold p = new QsiPortControllerScaffold();
        c.connectPort(p);

        // start monitor
        QsiListenerScaffold s = new QsiListenerScaffold();
        c.addQsiListener(s);

        // send a message
        QsiMessage m = new QsiMessage(3);
        m.setElement(0, 11);
        m.setElement(1, 0);
        m.setElement(2, 0);
        c.sendQsiMessage(m, new QsiListenerScaffold());
        JUnitUtil.waitFor(100);

        // check it arrived at monitor
        assertNotNull( s.rcvdMsg, "message not null");
        assertEquals( 6, tostream.available(), "total length ");
        assertEquals( (byte)'S', tostream.readByte(), "Lead S");
        assertEquals( 11, tostream.readByte(), "Byte 0");
        assertEquals( 0, tostream.readByte(), "Byte 1");
        assertEquals( 0, tostream.readByte(), "Byte 2");
        assertEquals( 11, tostream.readByte(), "Check");
        assertEquals( (byte)'E', tostream.readByte(), "E");
        assertEquals( 0, tostream.available(), "remaining");
    }

    @Test
    public void testRcvReply() throws IOException {
        QsiTrafficController c = new QsiTrafficController() {
            // skip timeout message
            //protected void handleTimeout(jmri.jmrix.AbstractMRMessage msg,jmri.jmrix.AbstractMRListener l) {};
            //public void receiveLoop() {}
            //protected void portWarn(Exception e) {}
        };

        // connect to iostream via port controller
        QsiPortControllerScaffold p = new QsiPortControllerScaffold();
        c.connectPort(p);

        // object to receive reply
        QsiListenerScaffold l = new QsiListenerScaffold();
        c.addQsiListener(l);

        // send a message
        QsiMessage m = new QsiMessage(3);
        m.setOpCode('0');
        m.setElement(1, '1');
        m.setElement(2, '2');
        c.sendQsiMessage(m, l);
        // that's already tested, so don't do here.

        // now send reply
        tistream.write('S');
        tistream.write(0);
        tistream.write(0);
        tistream.write('E');

        // drive the mechanism
        c.handleOneIncomingReply();

        JUnitUtil.waitFor(() -> { return l.rcvdReply != null; }, "reply received");
        assertEquals( 'S', l.rcvdReply.getOpCode(), "first char of reply ");
    }

    // internal class to simulate a QsiPortController
    private class QsiPortControllerScaffold extends QsiPortController {

        QsiPortControllerScaffold() {
            super(new QsiSystemConnectionMemo());
            assertDoesNotThrow( () -> {
                PipedInputStream tempPipe = new PipedInputStream();
                tostream = new DataInputStream(tempPipe);
                ostream = new DataOutputStream(new PipedOutputStream(tempPipe));
                tempPipe = new PipedInputStream();
                istream = new DataInputStream(tempPipe);
                tistream = new DataOutputStream(new PipedOutputStream(tempPipe));
            });
        }

        @Override
        public Vector<String> getPortNames() {
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

    // from here down is testing infrastructure

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(QsiTrafficControllerTest.class);

}
