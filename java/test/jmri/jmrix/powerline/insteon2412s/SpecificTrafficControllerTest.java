package jmri.jmrix.powerline.insteon2412s;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import jmri.jmrix.powerline.SerialListener;
import jmri.jmrix.powerline.SerialMessage;
import jmri.jmrix.powerline.SerialPortController;
import jmri.jmrix.powerline.SerialReply;
import jmri.jmrix.powerline.SerialSystemConnectionMemo;
import jmri.jmrix.powerline.SerialTrafficController;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JUnit tests for the SpecificTrafficController class.
 *
 * @author	Bob Jacobsen Copyright 2005, 2007, 2008, 2009 Converted to multiple
 * connection
 * @author kcameron Copyright (C) 2011
 */
public class SpecificTrafficControllerTest extends jmri.jmrix.powerline.SerialTrafficControllerTest {

    SerialTrafficController t = null;
    SerialSystemConnectionMemo m = null;

    // inner class to give access to protected endOfMessage method
    class TestSerialTC extends SpecificTrafficController {

        public TestSerialTC(SerialSystemConnectionMemo m) {
            super(m);
        }

        boolean testEndOfMessage(SerialReply r) {
            return endOfMessage(r);
        }

        @Override
        protected void forwardToPort(jmri.jmrix.AbstractMRMessage m, jmri.jmrix.AbstractMRListener reply) {
        }
    }

    @Test
    public void testReceiveStates1() {
        TestSerialTC c = new TestSerialTC(m);
        SerialReply r = new SpecificReply(t);

        r.setElement(0, Constants.HEAD_STX);
        Assert.assertTrue("STX", !c.testEndOfMessage(r));

        r.setElement(1, Constants.POLL_REQ_BUTTON_RESET);
        Assert.assertTrue("Reset", c.testEndOfMessage(r));
    }

    @Test
    public void testReceiveStatesRead() {
        // build a dim for address 11.22.33 to 50%
        TestSerialTC c = new TestSerialTC(m);
        SerialReply r = new SpecificReply(t);

        r.setElement(0, Constants.HEAD_STX);
        Assert.assertTrue("wait for read", !c.testEndOfMessage(r));

        r.setElement(1, Constants.POLL_REQ_STD);
        Assert.assertTrue("IM Command", !c.testEndOfMessage(r));

        r.setElement(2, 0x11);
        Assert.assertTrue("Addr High", !c.testEndOfMessage(r));

        r.setElement(3, 0x22);
        Assert.assertTrue("Addr Middle", !c.testEndOfMessage(r));

        r.setElement(4, 0x33);
        Assert.assertTrue("Addr Low", !c.testEndOfMessage(r));

        r.setElement(5, 0x44);
        Assert.assertTrue("Addr High", !c.testEndOfMessage(r));

        r.setElement(6, 0x55);
        Assert.assertTrue("Addr Middle", !c.testEndOfMessage(r));

        r.setElement(7, 0x66);
        Assert.assertTrue("Addr Low", !c.testEndOfMessage(r));

        r.setElement(8, 0x00);
        Assert.assertTrue("Flags", !c.testEndOfMessage(r));

        r.setElement(9, Constants.CMD_LIGHT_CHG);
        Assert.assertTrue("Dim Cmd", !c.testEndOfMessage(r));

        r.setElement(10, 0x80);
        Assert.assertTrue("50% dim", c.testEndOfMessage(r));

        // and next reply OK
        //r.setElement(8, Constants.REPLY_ACK);        
        //Assert.assertTrue("single byte reply", c.testEndOfMessage(r));
    }

      
//    public void testSerialNodeEnumeration() {
//        SpecificTrafficController c = new SpecificTrafficController();
//        SerialNode b = new SerialNode(1,SerialNode.DAUGHTER);
//        SerialNode f = new SerialNode(3,SerialNode.CABDRIVER);
//        SerialNode d = new SerialNode(2,SerialNode.CABDRIVER);
//        SerialNode e = new SerialNode(6,SerialNode.DAUGHTER);
//        Assert.assertEquals("1st Node", b, c.getSerialNode(0) );
//        Assert.assertEquals("2nd Node", f, c.getSerialNode(1) );
//        Assert.assertEquals("3rd Node", d, c.getSerialNode(2) );
//        Assert.assertEquals("4th Node", e, c.getSerialNode(3) );
//        Assert.assertEquals("no more Nodes", null, c.getSerialNode(4) );
//        Assert.assertEquals("1st Node Again", b, c.getSerialNode(0) );
//        Assert.assertEquals("2nd Node Again", f, c.getSerialNode(1) );
//        Assert.assertEquals("node with address 6", e, c.getNodeFromAddress(6) );
//        Assert.assertEquals("3rd Node again", d, c.getSerialNode(2) );
//        Assert.assertEquals("no node with address 0", null, c.getNodeFromAddress(0) );
//        c.deleteSerialNode(6);
//        Assert.assertEquals("1st Node after del", b, c.getSerialNode(0) );
//        Assert.assertEquals("2nd Node after del", f, c.getSerialNode(1) );
//        Assert.assertEquals("3rd Node after del", d, c.getSerialNode(2) );
//        Assert.assertEquals("no more Nodes after del", null, c.getSerialNode(3) );
//        c.deleteSerialNode(1);
//        jmri.util.JUnitAppender.assertWarnMessage("Deleting the serial node active in the polling loop");
//        Assert.assertEquals("1st Node after del2", f, c.getSerialNode(0) );
//        Assert.assertEquals("2nd Node after del2", d, c.getSerialNode(1) );
//        Assert.assertEquals("no more Nodes after del2", null, c.getSerialNode(2) );        
//    }
/*     public void testSerialOutput() { */
    /*         SerialTrafficController c = new SerialTrafficController(); */
    /*         SerialNode a = new SerialNode(); */
    /*         SerialNode g = new SerialNode(5,SerialNode.DAUGHTER); */
    /*         Assert.assertTrue("must Send", g.mustSend() ); */
    /*         g.resetMustSend(); */
    /*         Assert.assertTrue("must Send off", !(g.mustSend()) ); */
    /*         //c.setSerialOutput("PL5B2",false); // test and 12 year old method removed, called nowhere as of 4.9.4 */
    /*         SerialMessage m = g.createOutPacket(); */
    /*         Assert.assertEquals("packet size", 9, m.getNumDataElements() ); */
    /*         Assert.assertEquals("node address", 5, m.getElement(0) ); */
    /*         Assert.assertEquals("byte 1 lo nibble", 0x02, m.getElement(1) );       */
    /*         Assert.assertEquals("byte 1 hi nibble", 0x10, m.getElement(2) );       */
    /*         Assert.assertEquals("byte 2 lo nibble", 0x20, m.getElement(3) );       */
    /*         Assert.assertEquals("byte 2 hi nibble", 0x30, m.getElement(4) );       */
    /*         Assert.assertEquals("byte 3 lo nibble", 0x41, m.getElement(5) );       */
    /*         Assert.assertEquals("byte 3 hi nibble", 0x50, m.getElement(6) );       */
    /*         Assert.assertEquals("byte 4 lo nibble", 0x60, m.getElement(7) );       */
    /*         Assert.assertEquals("byte 4 hi nibble", 0x70, m.getElement(8) );       */
    /*     } */
    @SuppressWarnings("unused")
    private boolean waitForReply() {
        // wait for reply (normally, done by callback; will check that later)
        int i = 0;
        while (rcvdReply == null && i++ < 100) {
            try {
                Thread.sleep(10);
            } catch (Exception e) {
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("past loop, i=" + i
                    + " reply=" + rcvdReply);
        }
        if (i == 0) {
            log.warn("waitForReply saw an immediate return; is threading right?");
        }
        return i < 100;
    }

    // internal class to simulate a Listener
    class SerialListenerScaffold implements SerialListener {

        public SerialListenerScaffold() {
            rcvdReply = null;
            rcvdMsg = null;
        }

        @Override
        public void message(SerialMessage m) {
            rcvdMsg = m;
        }

        @Override
        public void reply(SerialReply r) {
            rcvdReply = r;
        }
    }
    SerialReply rcvdReply;
    SerialMessage rcvdMsg;

    // internal class to simulate a PortController
    class SerialPortControllerScaffold extends SerialPortController {

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
            return new String[] {};
        }

        //@Override
        @Override
        public int[] validBaudNumbers() {
            return new int[] {};
        }

        protected SerialPortControllerScaffold() throws Exception {
            super(null);
            PipedInputStream tempPipe;
            tempPipe = new PipedInputStream();
            tostream = new DataInputStream(tempPipe);
            ostream = new DataOutputStream(new PipedOutputStream(tempPipe));
            tempPipe = new PipedInputStream();
            istream = new DataInputStream(tempPipe);
            tistream = new DataOutputStream(new PipedOutputStream(tempPipe));
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
    static DataOutputStream ostream;  // Traffic controller writes to this
    static DataInputStream tostream; // so we can read it from this

    static DataOutputStream tistream; // tests write to this
    static DataInputStream istream;  // so the traffic controller can read from this

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        m = new SpecificSystemConnectionMemo();
        tc = t = new SpecificTrafficController(m);
    }

    @Override
    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

    private final static Logger log = LoggerFactory.getLogger(SpecificTrafficControllerTest.class);

}
