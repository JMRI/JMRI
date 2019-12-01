package jmri.jmrix.cmri.serial;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Vector;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description:	JUnit tests for the SerialTrafficController class
 *
 * @author	Bob Jacobsen Copyright 2006
 */
public class SerialTrafficControllerTest extends jmri.jmrix.AbstractMRNodeTrafficControllerTest {


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
    class SerialListenerScaffold implements jmri.jmrix.cmri.serial.SerialListener {

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

    // internal class to simulate a PortAdapter
    class SerialPortAdapterScaffold extends SerialPortAdapter {

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

        protected SerialPortAdapterScaffold() throws Exception {
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
        tc = new SerialTrafficController();
    }

    @Override
    @After
    public void tearDown() {
        tc = null;
	    JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(SerialTrafficControllerTest.class);

}
