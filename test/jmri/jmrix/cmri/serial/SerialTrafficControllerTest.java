/**
 * SerialTrafficControllerTest.java
 *
 * Description:	    JUnit tests for the SerialTrafficController class
 * @author			Bob Jacobsen
 * @version $Revision: 1.6 $
 */

package jmri.jmrix.cmri.serial;

import jmri.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import junit.framework.Test;
import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class SerialTrafficControllerTest extends TestCase {

    public void testCreate() {
        SerialTrafficController m = new SerialTrafficController();
    }
    
    public void testSerialNodeEnumeration() {
        SerialTrafficController c = new SerialTrafficController();
        SerialNode b = new SerialNode(1,SerialNode.USIC_SUSIC);
        SerialNode f = new SerialNode(3,SerialNode.SMINI);
        SerialNode d = new SerialNode(2,SerialNode.SMINI);
        SerialNode e = new SerialNode(6,SerialNode.USIC_SUSIC);
        Assert.assertEquals("1st Node", b, c.getSerialNode(0) );
        Assert.assertEquals("2nd Node", f, c.getSerialNode(1) );
        Assert.assertEquals("3rd Node", d, c.getSerialNode(2) );
        Assert.assertEquals("4th Node", e, c.getSerialNode(3) );
        Assert.assertEquals("no more Nodes", null, c.getSerialNode(4) );
        Assert.assertEquals("1st Node Again", b, c.getSerialNode(0) );
        Assert.assertEquals("2nd Node Again", f, c.getSerialNode(1) );
        Assert.assertEquals("node with address 6", e, c.getNodeFromAddress(6) );
        Assert.assertEquals("3rd Node again", d, c.getSerialNode(2) );
        Assert.assertEquals("no node with address 0", null, c.getNodeFromAddress(0) );
        c.deleteSerialNode(6);
        Assert.assertEquals("1st Node after del", b, c.getSerialNode(0) );
        Assert.assertEquals("2nd Node after del", f, c.getSerialNode(1) );
        Assert.assertEquals("3rd Node after del", d, c.getSerialNode(2) );
        Assert.assertEquals("no more Nodes after del", null, c.getSerialNode(3) );
        c.deleteSerialNode(1);
        Assert.assertEquals("1st Node after del2", f, c.getSerialNode(0) );
        Assert.assertEquals("2nd Node after del2", d, c.getSerialNode(1) );
        Assert.assertEquals("no more Nodes after del2", null, c.getSerialNode(2) );        
    }
    public void testSerialOutput() {
        SerialTrafficController c = new SerialTrafficController();
        SerialNode a = new SerialNode();
        SerialNode g = new SerialNode(5,SerialNode.SMINI);
        Assert.assertTrue("must Send", g.mustSend() );
        g.resetMustSend();
        Assert.assertTrue("must Send off", !(g.mustSend()) );
        c.setSerialOutput("CL5B2",false);
        c.setSerialOutput("CL5B1",false);
        c.setSerialOutput("CL5B23",false);
        c.setSerialOutput("CL5B41",false);
        c.setSerialOutput("CL5B25",false);
        c.setSerialOutput("CL5B2",true);
        c.setSerialOutput("CL5B19",false);
        c.setSerialOutput("CL5B5",false);
        c.setSerialOutput("CL5B26",false);
        c.setSerialOutput("CL5B48",false);
        Assert.assertTrue("must Send on", g.mustSend() );
        SerialMessage m = g.createOutPacket();
        Assert.assertEquals("packet size", 9, m.getNumDataElements() );
        Assert.assertEquals("node address", 70, m.getElement(0) );
        Assert.assertEquals("packet type", 84, m.getElement(1) );  // 'T'        
        Assert.assertEquals("out byte 1", 17, (m.getElement(2) & 0xff));      
        Assert.assertEquals("out byte 2", 0, (m.getElement(3) & 0xff));      
        Assert.assertEquals("out byte 3", 68, (m.getElement(4) & 0xff));      
        Assert.assertEquals("DLE", 16, (m.getElement(5) & 0xff));      
        Assert.assertEquals("out byte 4", 3, (m.getElement(6) & 0xff));      
        Assert.assertEquals("out byte 5", 0, (m.getElement(7) & 0xff));      
        Assert.assertEquals("out byte 6", 129, (m.getElement(8) & 0xff));      
    }

    private boolean waitForReply() {
        // wait for reply (normally, done by callback; will check that later)
        int i = 0;
        while ( rcvdReply == null && i++ < 100  )  {
            try {
                Thread.sleep(10);
            } catch (Exception e) {
            }
        }
        if (log.isDebugEnabled()) log.debug("past loop, i="+i
                                            +" reply="+rcvdReply);
        if (i==0) log.warn("waitForReply saw an immediate return; is threading right?");
        return i<100;
    }

    // internal class to simulate a Listener
    class SerialListenerScaffold implements jmri.jmrix.cmri.serial.SerialListener {
        public SerialListenerScaffold() {
            rcvdReply = null;
            rcvdMsg = null;
        }
        public void message(SerialMessage m) {rcvdMsg = m;}
        public void reply(SerialReply r) {rcvdReply = r;}
    }
    SerialReply rcvdReply;
    SerialMessage rcvdMsg;

    // internal class to simulate a PortController
    class SerialPortControllerScaffold extends SerialPortController {
            public java.util.Vector getPortNames() { return null; }
	    public String openPort(String portName, String appName) { return null; }
	    public void configure() {}
	    public String[] validBaudRates() { return null; }
        protected SerialPortControllerScaffold() throws Exception {
            PipedInputStream tempPipe;
            tempPipe = new PipedInputStream();
            tostream = new DataInputStream(tempPipe);
            ostream = new DataOutputStream(new PipedOutputStream(tempPipe));
            tempPipe = new PipedInputStream();
            istream = new DataInputStream(tempPipe);
            tistream = new DataOutputStream(new PipedOutputStream(tempPipe));
        }

        // returns the InputStream from the port
        public DataInputStream getInputStream() { return istream; }

        // returns the outputStream to the port
        public DataOutputStream getOutputStream() { return ostream; }

        // check that this object is ready to operate
        public boolean status() { return true; }
    }
    static DataOutputStream ostream;  // Traffic controller writes to this
    static DataInputStream  tostream; // so we can read it from this

    static DataOutputStream tistream; // tests write to this
    static DataInputStream  istream;  // so the traffic controller can read from this

    // from here down is testing infrastructure

    public SerialTrafficControllerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SerialTrafficControllerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SerialTrafficControllerTest.class);
        return suite;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialTrafficControllerTest.class.getName());

}
