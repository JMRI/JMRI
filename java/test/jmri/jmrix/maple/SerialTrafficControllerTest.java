package jmri.jmrix.maple;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Vector;

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
        SerialNode b = new SerialNode(1, 0,c);
        SerialNode f = new SerialNode(3, 0,c);
        SerialNode d = new SerialNode(2, 0,c);
        SerialNode e = new SerialNode(6, 0,c);
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

    @Test
    public void testListenerScaffold() {
        SerialListener listener = new SerialListenerScaffold();
        ((SerialTrafficController)tc).addSerialListener(listener);
        ((SerialTrafficController)tc).removeSerialListener(listener);
        Assert.assertNotNull(listener); // just checking no exceptions thrown
    }

    @Test
    public void testScaffold() throws java.io.IOException {
        SerialPortControllerScaffold scaff = new SerialPortControllerScaffold();

        Assertions.assertNotNull(scaff);
        Assertions.assertNotNull(tostream);
        Assertions.assertNotNull(ostream);
        Assertions.assertNotNull(istream);
        Assertions.assertNotNull(tistream);

        scaff.dispose();
    }
    
    // internal class to simulate a Listener
    private static class SerialListenerScaffold implements jmri.jmrix.maple.SerialListener {

        SerialListenerScaffold() {
        }

        @Override
        public void message(SerialMessage m) {
        }

        @Override
        public void reply(SerialReply r) {
        }
    }

    // internal class to simulate a PortController
    private class SerialPortControllerScaffold extends SerialPortController {

        SerialPortControllerScaffold() throws java.io.IOException {
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

    private MapleSystemConnectionMemo memo;

    @Override
    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        memo = new MapleSystemConnectionMemo();
        tc = new SerialTrafficController();
        memo.setTrafficController((SerialTrafficController)tc);
    }

    @Override
    @AfterEach
    public void tearDown() {
        if ( memo !=null ) {
            memo.dispose();
            memo = null;
        }
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(SerialTrafficControllerTest.class);

}
