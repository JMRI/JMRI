package jmri.jmrix.cmri.serial;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Vector;

import jmri.jmrix.cmri.CMRISystemConnectionMemo;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * JUnit tests for the SerialTrafficController class
 *
 * @author Bob Jacobsen Copyright 2006
 */
public class SerialTrafficControllerTest extends jmri.jmrix.AbstractMRNodeTrafficControllerTest {

    @Test
    public void testSerialNodeEnumeration() {
        SerialTrafficController c = (SerialTrafficController)tc;
        SerialNode b = new SerialNode(1, SerialNode.USIC_SUSIC,c);
        SerialNode f = new SerialNode(3, SerialNode.SMINI,c);
        SerialNode d = new SerialNode(2, SerialNode.SMINI,c);
        SerialNode e = new SerialNode(6, SerialNode.USIC_SUSIC,c);
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
        JUnitAppender.assertWarnMessage("Deleting the serial node active in the polling loop");

        Assert.assertEquals("1st Node after del2", f, c.getNode(0));
        Assert.assertEquals("2nd Node after del2", d, c.getNode(1));
        Assert.assertEquals("no more Nodes after del2", null, c.getNode(2));
    }

    @Test
    public void testListenerScaffold() {
        SerialListenerScaffold listener = new SerialListenerScaffold();

        SerialMessage msg = new SerialMessage(4);
        msg.setOpCode(0x81);
        msg.setElement(1, 0x02);
        msg.setElement(2, 0xA2);
        msg.setElement(3, 0x00);
        msg.setBinary(true);
        
        SerialReply reply = new SerialReply("Q01");
        
        listener.message(msg);
        Assertions.assertEquals(msg, rcvdMsg);
        
        listener.reply(reply);
        Assertions.assertEquals(reply, rcvdReply);
    }

    @Test
    public void testScaffold() throws java.io.IOException {
        SerialPortAdapterScaffold scaff = new SerialPortAdapterScaffold();

        Assertions.assertNotNull(scaff);
        Assertions.assertNotNull(tostream);
        Assertions.assertNotNull(ostream);
        Assertions.assertNotNull(istream);
        Assertions.assertNotNull(tistream);

        scaff.dispose();
    }

    // internal class to simulate a Listener
    private class SerialListenerScaffold implements jmri.jmrix.cmri.serial.SerialListener {

        SerialListenerScaffold() {
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

    // internal class to simulate a PortAdapter
    private class SerialPortAdapterScaffold extends SerialPortAdapter {

        SerialPortAdapterScaffold() throws java.io.IOException {
            super(memo);
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

    private CMRISystemConnectionMemo memo;

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new CMRISystemConnectionMemo();
        tc = new SerialTrafficController();
        memo.setTrafficController((SerialTrafficController)tc);
    }

    @Override
    @AfterEach
    public void tearDown() {
        tc = null;
        if ( memo != null ) {
            memo.dispose();
            memo = null;
        }
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SerialTrafficControllerTest.class);

}
