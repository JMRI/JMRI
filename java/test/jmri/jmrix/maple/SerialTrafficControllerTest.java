package jmri.jmrix.maple;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Vector;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description:	JUnit tests for the SerialTrafficController class
 *
 * @author	Bob Jacobsen Copyright 2006
 */
public class SerialTrafficControllerTest extends TestCase {

    public void testCreate() {
        SerialTrafficController m = new SerialTrafficController();
        Assert.assertNotNull("exists", m);
    }

    public void testSerialNodeEnumeration() {
        SerialTrafficController c = new SerialTrafficController();
        SerialNode b = new SerialNode(1, 0);
        SerialNode f = new SerialNode(3, 0);
        SerialNode d = new SerialNode(2, 0);
        SerialNode e = new SerialNode(6, 0);
        Assert.assertEquals("1st Node", b, c.getNode(0));
        Assert.assertEquals("2nd Node", f, c.getNode(1));
        Assert.assertEquals("3rd Node", d, c.getNode(2));
        Assert.assertEquals("4th Node", e, c.getNode(3));
        Assert.assertEquals("no more Nodes", null, c.getNode(4));
        Assert.assertEquals("1st Node Again", b, c.getNode(0));
        Assert.assertEquals("2nd Node Again", f, c.getNode(1));
        Assert.assertEquals("3rd Node again", d, c.getNode(2));

        c.deleteNode(6);

        Assert.assertEquals("1st Node after del", b, c.getNode(0));
        Assert.assertEquals("2nd Node after del", f, c.getNode(1));
        Assert.assertEquals("3rd Node after del", d, c.getNode(2));
        Assert.assertEquals("no more Nodes after del", null, c.getNode(3));
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
    class SerialListenerScaffold implements jmri.jmrix.maple.SerialListener {

        public SerialListenerScaffold() {
            rcvdReply = null;
            rcvdMsg = null;
        }

        public void message(SerialMessage m) {
            rcvdMsg = m;
        }

        public void reply(SerialReply r) {
            rcvdReply = r;
        }
    }
    SerialReply rcvdReply;
    SerialMessage rcvdMsg;

    // internal class to simulate a PortController
    class SerialPortControllerScaffold extends SerialPortController {

        public Vector<String> getPortNames() {
            return null;
        }

        public String openPort(String portName, String appName) {
            return null;
        }

        public void configure() {
        }

        public String[] validBaudRates() {
            return null;
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
        public DataInputStream getInputStream() {
            return istream;
        }

        // returns the outputStream to the port
        public DataOutputStream getOutputStream() {
            return ostream;
        }

        // check that this object is ready to operate
        public boolean status() {
            return true;
        }
    }
    static DataOutputStream ostream;  // Traffic controller writes to this
    static DataInputStream tostream; // so we can read it from this

    static DataOutputStream tistream; // tests write to this
    static DataInputStream istream;  // so the traffic controller can read from this

    // from here down is testing infrastructure
    public SerialTrafficControllerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", SerialTrafficControllerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SerialTrafficControllerTest.class);
        return suite;
    }

    // The minimal setup for log4J
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(SerialTrafficControllerTest.class.getName());

}
