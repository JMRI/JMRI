package jmri.jmrix.acela;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Vector;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * JUnit tests for the AcelaTrafficController class.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2007, 2015
 */
public class AcelaTrafficControllerTest extends jmri.jmrix.AbstractMRNodeTrafficControllerTest {

    public void testSendThenRcvReply() throws Exception {
        AcelaTrafficController c = (AcelaTrafficController)tc;
        // connect to iostream via port controller
        AcelaPortControllerScaffold p = new AcelaPortControllerScaffold();
        c.connectPort(p);

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
        Assert.assertEquals("total length ", 4, tostream.available());
        Assert.assertEquals("Char 0", (byte)'0', tostream.readByte());
        Assert.assertEquals("Char 1", (byte)'1', tostream.readByte());
        Assert.assertEquals("Char 2", (byte)'2', tostream.readByte());
        Assert.assertEquals("EOM", 0x0d, tostream.readByte());


        // now send reply
        tistream.write('P');
        tistream.write(0x0d);

        // drive the mechanism
        c.handleOneIncomingReply();

        JUnitUtil.waitFor(()->{return rcvdReply != null;}, "reply received");

        Assert.assertTrue("reply received ", rcvdReply != null);
        Assert.assertEquals("first char of reply ", 'P', rcvdReply.getOpCode());
    }

    @Test
    public void testListenerRcvdMsg() {
        AcelaMessage m = new AcelaMessage(3);
        m.setOpCode('0');
        m.setElement(1, '1');
        m.setElement(2, '2');

        AcelaListener l = new AcelaListenerScaffold();
        l.message(m);
        Assertions.assertEquals(m, rcvdMsg);
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
    AcelaReply rcvdReply;
    AcelaMessage rcvdMsg;

    // internal class to simulate an AcelaPortController
    private class AcelaPortControllerScaffold extends AcelaPortController {

        AcelaPortControllerScaffold() throws Exception {
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
    DataOutputStream ostream;  // Traffic controller writes to this
    DataInputStream tostream; // so we can read it from this

    DataOutputStream tistream; // tests write to this
    DataInputStream istream;  // so the traffic controller can read from this

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        tc = new AcelaTrafficController();
    }

    @Override
    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

}
