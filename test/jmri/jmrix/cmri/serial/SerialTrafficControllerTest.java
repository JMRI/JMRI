/**
 * SerialTrafficControllerTest.java
 *
 * Description:	    JUnit tests for the SerialTrafficController class
 * @author			Bob Jacobsen
 * @version $Revision: 1.1 $
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
    
    public void testSendAscii() throws Exception {
        SerialTrafficController c = new SerialTrafficController();
        
        // connect to iostream via port controller
        SerialPortControllerScaffold p = new SerialPortControllerScaffold();
        c.connectPort(p);
        
        // send a message
        SerialMessage m = new SerialMessage(3);
        m.setBinary(false);
        m.setOpCode('0');
        m.setElement(1, '1');
        m.setElement(2, '2');
        c.sendSerialMessage(m, new SerialListenerScaffold());
        Assert.assertEquals("total length ", 4, tostream.available());
        Assert.assertEquals("Char 0", '0', tostream.readByte());
        Assert.assertEquals("Char 1", '1', tostream.readByte());
        Assert.assertEquals("Char 2", '2', tostream.readByte());
        Assert.assertEquals("EOM", 0x0d, tostream.readByte());
        Assert.assertEquals("remaining ", 0, tostream.available());
    }
    
    public void testSendBinary() throws Exception {
        SerialTrafficController c = new SerialTrafficController();
        
        // connect to iostream via port controller
        SerialPortControllerScaffold p = new SerialPortControllerScaffold();
        c.connectPort(p);
        
        // send a message
        SerialMessage m = new SerialMessage(3);
        m.setBinary(true);
        m.setOpCode(0x81);
        m.setElement(1, 0x12);
        m.setElement(2, 0x34);
        c.sendSerialMessage(m, new SerialListenerScaffold());
        Assert.assertEquals("total length ", 3, tostream.available());
        Assert.assertEquals("Char 0", 0x81, 0xFF & tostream.readByte());
        Assert.assertEquals("Char 1", 0x12, tostream.readByte());
        Assert.assertEquals("Char 2", 0x34, tostream.readByte());
        Assert.assertEquals("remaining ", 0, tostream.available());
    }
    
    public void testMonitor() throws Exception {
        SerialTrafficController c = new SerialTrafficController();
        
        // connect to iostream via port controller
        SerialPortControllerScaffold p = new SerialPortControllerScaffold();
        c.connectPort(p);
        
        // start monitor
        rcvdMsg = null;
        SerialListenerScaffold s = new SerialListenerScaffold();
        c.addSerialListener(s);
        
        // send a message
        SerialMessage m = new SerialMessage(3);
        m.setOpCode('0');
        m.setElement(1, '1');
        m.setElement(2, '2');
        c.sendSerialMessage(m, new SerialListenerScaffold());
        
        // check it arrived at monitor
        Assert.assertTrue("message not null", rcvdMsg != null);
        Assert.assertEquals("total length ", 4, tostream.available());
        Assert.assertEquals("Char 0", '0', tostream.readByte());
        Assert.assertEquals("Char 1", '1', tostream.readByte());
        Assert.assertEquals("Char 2", '2', tostream.readByte());
        Assert.assertEquals("EOM", 0x0d, tostream.readByte());
        Assert.assertEquals("remaining ", 0, tostream.available());
    }
    
    public void testRcvReply() throws Exception {
        SerialTrafficController c = new SerialTrafficController();
        
        // connect to iostream via port controller
        SerialPortControllerScaffold p = new SerialPortControllerScaffold();
        c.connectPort(p);
        
        // object to receive reply
        SerialListener l = new SerialListenerScaffold();
        c.addSerialListener(l);
        
        // send a message
        SerialMessage m = new SerialMessage(3);
        m.setOpCode('0');
        m.setElement(1, '1');
        m.setElement(2, '2');
        c.sendSerialMessage(m, l);
        // that's already tested, so don't do here.
        
        // now send reply
        tistream.write('R');
        tistream.write(0x0d);
        tistream.write('C');
        tistream.write('O');
        tistream.write('M');
        tistream.write('M');
        tistream.write('A');
        tistream.write('N');
        tistream.write('D');
        tistream.write(':');
        tistream.write(' ');
        
        // drive the mechanism
        c.handleOneIncomingReply();
        Assert.assertTrue("reply received ", waitForReply());
        Assert.assertEquals("first char of reply ", 'R', rcvdReply.getOpCode());
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
    
    // internal class to simulate a NceListener
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
    
    // internal class to simulate a NcePortController
    class SerialPortControllerScaffold extends SerialPortController {
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
