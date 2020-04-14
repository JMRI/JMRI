package jmri.jmrix.grapevine;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JUnit tests for the SerialTrafficController class
 *
 * @author	Bob Jacobsen Copyright 2005, 2007, 2008
 */
public class SerialTrafficControllerTest extends jmri.jmrix.AbstractMRNodeTrafficControllerTest {

    byte[] testBuffer;
    boolean invoked;

    @Test
    public void testStateMachine1() throws java.io.IOException {
        SerialTrafficController c = (SerialTrafficController) tc;
        testBuffer = new byte[4];
        invoked = false;

        DataInputStream i = new DataInputStream(new ByteArrayInputStream(
                new byte[]{1, 2, 3, 4}));

        c.doNextStep(new SerialReply(), i);

        jmri.util.JUnitAppender.assertWarnMessage("1st byte not address: 1");
        Assert.assertEquals("not invoked", false, invoked);
    }

    @Test
    public void testStateMachine2() throws java.io.IOException {
        SerialTrafficController c = (SerialTrafficController) tc;
        testBuffer = new byte[4];
        invoked = false;

        DataInputStream i = new DataInputStream(new ByteArrayInputStream(
                new byte[]{(byte) 128, (byte) 129, 3, 4}));

        c.doNextStep(new SerialReply(), i);

        jmri.util.JUnitAppender.assertWarnMessage("2nd byte HOB set: 129, going to state 1");
        Assert.assertEquals("not invoked", false, invoked);
    }

    @Test
    public void testStateMachine3() throws java.io.IOException {
        SerialTrafficController c = (SerialTrafficController) tc;
        testBuffer = new byte[4];
        invoked = false;

        DataInputStream i = new DataInputStream(new ByteArrayInputStream(
                new byte[]{(byte) 128, (byte) 12, 1, 4}));

        c.doNextStep(new SerialReply(), i);

        jmri.util.JUnitAppender.assertWarnMessage("addresses don't match: 128, 1. going to state 1");
        Assert.assertEquals("not invoked", false, invoked);
    }

    @Test
    public void testStateMachine4() throws java.io.IOException {
        SerialTrafficController c = (SerialTrafficController) tc;
        testBuffer = new byte[4];
        invoked = false;

        DataInputStream i = new DataInputStream(new ByteArrayInputStream(
                new byte[]{(byte) 128, (byte) 12, (byte) 128, (byte) 129}));

        c.doNextStep(new SerialReply(), i);

        jmri.util.JUnitAppender.assertWarnMessage("3rd byte HOB set: 129, going to state 1");
        Assert.assertEquals("not invoked", false, invoked);
    }

    @Test
    public void testStateMachine5() throws java.io.IOException {
        SerialTrafficController c = (SerialTrafficController) tc;
        testBuffer = new byte[4];
        invoked = false;

        DataInputStream i = new DataInputStream(new ByteArrayInputStream(
                new byte[]{(byte) 129, (byte) 90, (byte) 129, (byte) 32}));


        c.doNextStep(new SerialReply(), i);

        jmri.util.JUnitAppender.assertWarnMessage("parity mismatch: 18, going to state 2 with content 129, 32");
        Assert.assertEquals("not invoked", false, invoked);
    }

    @Test
    public void testStateMachineOK1() throws java.io.IOException {
        SerialTrafficController c = (SerialTrafficController) tc;
        testBuffer = new byte[4];
        invoked = false;

        DataInputStream i = new DataInputStream(new ByteArrayInputStream(
                new byte[]{(byte) 129, (byte) 90, (byte) 129, (byte) 31}));

        c.doNextStep(new SerialReply(), i);

        Assert.assertEquals("invoked", true, invoked);
        Assert.assertEquals("byte 0", (byte) 129, testBuffer[0]);
        Assert.assertEquals("byte 1", (byte) 90, testBuffer[1]);
        Assert.assertEquals("byte 2", (byte) 129, testBuffer[2]);
        Assert.assertEquals("byte 3", (byte) 31, testBuffer[3]);
    }

    @Test
    public void testStateMachineOK2() throws java.io.IOException {
        SerialTrafficController c = (SerialTrafficController) tc;
        testBuffer = new byte[4];
        invoked = false;

        DataInputStream i = new DataInputStream(new ByteArrayInputStream(
                new byte[]{(byte) 0xE2, (byte) 119, (byte) 0xE2, (byte) 119}));

        c.doNextStep(new SerialReply(), i);

        Assert.assertEquals("invoked", true, invoked);
        Assert.assertEquals("byte 0", (byte) 0xE2, testBuffer[0]);
        Assert.assertEquals("byte 1", (byte) 119, testBuffer[1]);
        Assert.assertEquals("byte 2", (byte) 0xE2, testBuffer[2]);
        Assert.assertEquals("byte 3", (byte) 119, testBuffer[3]);
    }

    @Test
    public void testStateMachineOK3() throws java.io.IOException {
        SerialTrafficController c = (SerialTrafficController) tc;
        testBuffer = new byte[4];
        invoked = false;

        DataInputStream i = new DataInputStream(new ByteArrayInputStream(
                new byte[]{(byte) 0xE2, (byte) 13, (byte) 0xE2, (byte) 88}));

        c.doNextStep(new SerialReply(), i);

        Assert.assertEquals("invoked", true, invoked);
        Assert.assertEquals("byte 0", (byte) 0xE2, testBuffer[0]);
        Assert.assertEquals("byte 1", (byte) 13, testBuffer[1]);
        Assert.assertEquals("byte 2", (byte) 0xE2, testBuffer[2]);
        Assert.assertEquals("byte 3", (byte) 88, testBuffer[3]);
    }

    @Test
    public void testStateMachineOK4() throws java.io.IOException {
        SerialTrafficController c = (SerialTrafficController) tc;
        testBuffer = new byte[4];
        invoked = false;

        DataInputStream i = new DataInputStream(new ByteArrayInputStream(
                new byte[]{(byte) 0xE2, (byte) 14, (byte) 0xE2, (byte) 86}));

        c.doNextStep(new SerialReply(), i);

        Assert.assertEquals("invoked", true, invoked);
        Assert.assertEquals("byte 0", (byte) 0xE2, testBuffer[0]);
        Assert.assertEquals("byte 1", (byte) 14, testBuffer[1]);
        Assert.assertEquals("byte 2", (byte) 0xE2, testBuffer[2]);
        Assert.assertEquals("byte 3", (byte) 86, testBuffer[3]);
    }

    @Test
    public void testStateMachineOK5() throws java.io.IOException {
        SerialTrafficController c = (SerialTrafficController) tc;
        testBuffer = new byte[4];
        invoked = false;

        DataInputStream i = new DataInputStream(new ByteArrayInputStream(
                new byte[]{(byte) 0xE2, (byte) 15, (byte) 0xE2, (byte) 84}));

        c.doNextStep(new SerialReply(), i);

        Assert.assertEquals("invoked", true, invoked);
        Assert.assertEquals("byte 0", (byte) 0xE2, testBuffer[0]);
        Assert.assertEquals("byte 1", (byte) 15, testBuffer[1]);
        Assert.assertEquals("byte 2", (byte) 0xE2, testBuffer[2]);
        Assert.assertEquals("byte 3", (byte) 84, testBuffer[3]);
    }

    @Test
    public void testStateMachineRecover1() throws java.io.IOException {
        SerialTrafficController c = (SerialTrafficController) tc;
        testBuffer = new byte[4];
        invoked = false;

        DataInputStream i = new DataInputStream(new ByteArrayInputStream(
                new byte[]{12, (byte) 129, (byte) 90, (byte) 129, (byte) 31}));

        c.doNextStep(new SerialReply(), i);
        c.doNextStep(new SerialReply(), i);

        jmri.util.JUnitAppender.assertWarnMessage("1st byte not address: 12");
        Assert.assertEquals("invoked", true, invoked);
        Assert.assertEquals("byte 0", (byte) 129, testBuffer[0]);
        Assert.assertEquals("byte 1", (byte) 90, testBuffer[1]);
        Assert.assertEquals("byte 2", (byte) 129, testBuffer[2]);
        Assert.assertEquals("byte 3", (byte) 31, testBuffer[3]);
    }

    @Test
    public void testStateMachineRecover2() throws java.io.IOException {
        SerialTrafficController c = (SerialTrafficController) tc;
        testBuffer = new byte[4];
        invoked = false;

        DataInputStream i = new DataInputStream(new ByteArrayInputStream(
                new byte[]{(byte) 129, (byte) 129, (byte) 90, (byte) 129, (byte) 31}));

        c.doNextStep(new SerialReply(), i);
        c.doNextStep(new SerialReply(), i);

        jmri.util.JUnitAppender.assertWarnMessage("2nd byte HOB set: 129, going to state 1");
        Assert.assertEquals("invoked", true, invoked);
        Assert.assertEquals("byte 0", (byte) 129, testBuffer[0]);
        Assert.assertEquals("byte 1", (byte) 90, testBuffer[1]);
        Assert.assertEquals("byte 2", (byte) 129, testBuffer[2]);
        Assert.assertEquals("byte 3", (byte) 31, testBuffer[3]);
    }

    @Test
    public void testStateMachineRecover3() throws java.io.IOException {
        SerialTrafficController c = (SerialTrafficController) tc;
        testBuffer = new byte[4];
        invoked = false;

        DataInputStream i = new DataInputStream(new ByteArrayInputStream(
                new byte[]{(byte) 128, (byte) 12, (byte) 129, (byte) 90, (byte) 129, (byte) 31}));

        c.doNextStep(new SerialReply(), i);
        c.doNextStep(new SerialReply(), i);

        jmri.util.JUnitAppender.assertWarnMessage("addresses don't match: 128, 129. going to state 1");
        Assert.assertEquals("invoked", true, invoked);
        Assert.assertEquals("byte 0", (byte) 129, testBuffer[0]);
        Assert.assertEquals("byte 1", (byte) 90, testBuffer[1]);
        Assert.assertEquals("byte 2", (byte) 129, testBuffer[2]);
        Assert.assertEquals("byte 3", (byte) 31, testBuffer[3]);
    }

    @Test
    public void testStateMachineRecover4() throws java.io.IOException {
        SerialTrafficController c = (SerialTrafficController) tc;
        testBuffer = new byte[4];
        invoked = false;

        DataInputStream i = new DataInputStream(new ByteArrayInputStream(
                new byte[]{(byte) 129, (byte) 12, (byte) 129, (byte) 90, (byte) 129, (byte) 31}));

        c.doNextStep(new SerialReply(), i);
        c.doNextStep(new SerialReply(), i);

        jmri.util.JUnitAppender.assertWarnMessage("parity mismatch: 25, going to state 2 with content 129, 90");
        Assert.assertEquals("invoked", true, invoked);
        Assert.assertEquals("byte 0", (byte) 129, testBuffer[0]);
        Assert.assertEquals("byte 1", (byte) 90, testBuffer[1]);
        Assert.assertEquals("byte 2", (byte) 129, testBuffer[2]);
        Assert.assertEquals("byte 3", (byte) 31, testBuffer[3]);
    }

    @Test
    public void testSerialNodeEnumeration() {
        SerialTrafficController c = (SerialTrafficController) tc;
        SerialNode b = new SerialNode(1, SerialNode.NODE2002V6, c);
        SerialNode f = new SerialNode(3, SerialNode.NODE2002V1, c);
        SerialNode d = new SerialNode(2, SerialNode.NODE2002V1, c);
        SerialNode e = new SerialNode(6, SerialNode.NODE2002V6, c);
        Assert.assertEquals("1st Node", b, c.getNode(0));
        Assert.assertEquals("2nd Node", f, c.getNode(1));
        Assert.assertEquals("3rd Node", d, c.getNode(2));
        Assert.assertEquals("4th Node", e, c.getNode(3));
        Assert.assertEquals("no more Nodes", null, c.getNode(4));
        Assert.assertEquals("1st Node Again", b, c.getNode(0));
        Assert.assertEquals("2nd Node Again", f, c.getNode(1));
        Assert.assertEquals("node with address 6", e, c.getNodeFromAddress(6));
        Assert.assertEquals("3rd Node again", d, c.getNode(2));
        Assert.assertEquals("no node with address 0", null, c.getNodeFromAddress(0));
        c.deleteNode(6);
        Assert.assertEquals("1st Node after del", b, c.getNode(0));
        Assert.assertEquals("2nd Node after del", f, c.getNode(1));
        Assert.assertEquals("3rd Node after del", d, c.getNode(2));
        Assert.assertEquals("no more Nodes after del", null, c.getNode(3));
        c.deleteNode(1);
        jmri.util.JUnitAppender.assertWarnMessage("Deleting the serial node active in the polling loop");
        Assert.assertEquals("1st Node after del2", f, c.getNode(0));
        Assert.assertEquals("2nd Node after del2", d, c.getNode(1));
        Assert.assertEquals("no more Nodes after del2", null, c.getNode(2));
    }

    @Test
    public void testSerialOutput() {
        SerialTrafficController c = (SerialTrafficController) tc;
        SerialNode a = new SerialNode(c);
        SerialNode g = new SerialNode(5, SerialNode.NODE2002V1, c);
        Assert.assertTrue("must Send", g.mustSend());
        g.resetMustSend();
        Assert.assertNotNull("exists", a);
        Assert.assertTrue("must Send off", !(g.mustSend()));
        //c.setSerialOutput("GL5B2", false); // test and 12 year old method removed, called nowhere as of 4.9.4
        AbstractMRMessage m = g.createOutPacket();
        Assert.assertEquals("packet size", 4, m.getNumDataElements());
        Assert.assertEquals("node address", 5, m.getElement(0));
        Assert.assertEquals("packet type", 17, m.getElement(1));  // 'T'        
    }

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
        jmri.util.JUnitUtil.setUp();
        tc = new SerialTrafficController(new GrapevineSystemConnectionMemo()) {
            @Override
            void loadBuffer(AbstractMRReply msg) {
                testBuffer[0] = buffer[0];
                testBuffer[1] = buffer[1];
                testBuffer[2] = buffer[2];
                testBuffer[3] = buffer[3];
                invoked = true;
            }
        };
    }

    @Override
    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

    private final static Logger log = LoggerFactory.getLogger(SerialTrafficControllerTest.class);

}
