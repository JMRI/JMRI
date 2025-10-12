package jmri.jmrix.acela;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.*;
import java.util.Vector;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * JUnit tests for the AcelaTrafficController class.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2007, 2015
 */
public class AcelaTrafficControllerTest extends jmri.jmrix.AbstractMRNodeTrafficControllerTest {

    @Test
    @Disabled("Test requires further development, times out in 5.13.4+")
    public void testSendThenRcvReply() throws IOException {
        AcelaTrafficController c = (AcelaTrafficController)tc;
        // connect to iostream via port controller
        assertDoesNotThrow( () -> {
            AcelaPortControllerScaffold p = new AcelaPortControllerScaffold();
            c.connectPort(p);
        });

        // object to receive reply
        AcelaListener l = new AcelaListenerScaffold();
        c.addAcelaListener(l);

        // send a message
        AcelaMessage m = new AcelaMessage(3);
        m.setOpCode('0');
        m.setElement(1, '1');
        m.setElement(2, '2');
        c.sendAcelaMessage(m, l);

        ostream.flush();
        JUnitUtil.waitFor(()->{return tostream.available() == 4;}, "total length");
        
        // test the result of sending
        assertEquals( 4, tostream.available(), "total length ");
        assertEquals( (byte)'0', tostream.readByte(), "Char 0");
        assertEquals( (byte)'1', tostream.readByte(), "Char 1");
        assertEquals( (byte)'2', tostream.readByte(), "Char 2");
        assertEquals( 0x0d, tostream.readByte(), "EOM");


        // now send reply
        tistream.write('P');
        tistream.write(0x0d);

        // drive the mechanism
        c.handleOneIncomingReply();

        JUnitUtil.waitFor(()->{return rcvdReply != null;}, "reply received");

        assertNotNull( rcvdReply, "reply received ");
        assertEquals( 'P', rcvdReply.getOpCode(), "first char of reply ");
    }

    @Test
    public void testListenerRcvdMsg() {
        AcelaMessage m = new AcelaMessage(3);
        m.setOpCode('0');
        m.setElement(1, '1');
        m.setElement(2, '2');

        AcelaListener l = new AcelaListenerScaffold();
        l.message(m);
        assertEquals(m, rcvdMsg);
    }

    // internal class to simulate an AcelaListener
    private class AcelaListenerScaffold implements AcelaListener {

        AcelaListenerScaffold() {
            rcvdReply = null;
            rcvdMsg = null;
        }

        @Override
        public void message(AcelaMessage m) {
            rcvdMsg = m;
        }

        @Override
        public void reply(AcelaReply r) {
            rcvdReply = r;
        }
    }

    private AcelaReply rcvdReply;
    private AcelaMessage rcvdMsg;

    // internal class to simulate an AcelaPortController
    private class AcelaPortControllerScaffold extends AcelaPortController {

        AcelaPortControllerScaffold() throws IOException {
            super(new AcelaSystemConnectionMemo());
            PipedInputStream tempPipe;
            tempPipe = new PipedInputStream();
            tostream = new DataInputStream(tempPipe);
            ostream = new DataOutputStream(new PipedOutputStream(tempPipe));
            tempPipe = new PipedInputStream();
            istream = new DataInputStream(tempPipe);
            tistream = new DataOutputStream(new PipedOutputStream(tempPipe));
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
            return new String[] {};
        }

        //@Override
        @Override
        public int[] validBaudNumbers() {
            return new int[] {};
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
        tc = new AcelaTrafficController();
    }

    @Override
    @AfterEach
    public void tearDown() {
        tc.terminateThreads();
        tc = null;
        JUnitUtil.tearDown();

    }

}
