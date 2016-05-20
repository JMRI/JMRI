// CbusProgrammerTest.java

package jmri.jmrix.can.cbus;

import org.apache.log4j.Logger;
import jmri.Programmer;
import jmri.ProgListener;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.jmrix.can.TestTrafficController;

/**
 * Tests for the jmri.jmrix.can.cbus.CbusProgrammer class.
 *
 * @author	Bob Jacobsen Copyright 2008
 * @version     $Revision$
 */
public class CbusProgrammerTest extends TestCase {

    public void testHasModes() {
        CbusProgrammer p = new CbusProgrammer(10, new TestTrafficController());
        
        Assert.assertTrue("CBUSNODEVARMODE", p.hasMode(Programmer.CBUSNODEVARMODE));
        Assert.assertTrue("PAGEMODE", !p.hasMode(Programmer.PAGEMODE));
        Assert.assertTrue("DIRECTBITMODE", !p.hasMode(Programmer.DIRECTBITMODE));
        Assert.assertTrue("OPSBYTEMODE", !p.hasMode(Programmer.OPSBYTEMODE));
        Assert.assertTrue("OPSACCBYTEMODE", !p.hasMode(Programmer.OPSACCBYTEMODE));
        Assert.assertTrue("OPSACCBITMODE", !p.hasMode(Programmer.OPSACCBITMODE));
        Assert.assertTrue("OPSACCEXTBYTEMODE", !p.hasMode(Programmer.OPSACCEXTBYTEMODE));
        Assert.assertTrue("OPSACCEXTBITMODE", !p.hasMode(Programmer.OPSACCEXTBITMODE));
    }

    public void testGetMode() {
        CbusProgrammer p = new CbusProgrammer(10, new TestTrafficController());
        
        Assert.assertEquals("CBUSNODEVARMODE", Programmer.CBUSNODEVARMODE, 
                                            p.getMode());
    }
    
    public void testSetMode() {
        CbusProgrammer p = new CbusProgrammer(10, new TestTrafficController());
        
        p.setMode(Programmer.PAGEMODE);
        
        Assert.assertEquals("CBUSNODEVARMODE", Programmer.CBUSNODEVARMODE, 
                                            p.getMode());
    }
    
    public void testGetCanRead() {
        CbusProgrammer p = new CbusProgrammer(10, new TestTrafficController());
        
        Assert.assertTrue("can read", p.getCanRead());
    }
    
    boolean reply;
    int rcvdValue;
    int rcvdStatus;
    
    ProgListener testListener = new ProgListener(){
        public void programmingOpReply(int value, int status) {
            reply = true;
            rcvdValue = value;
            rcvdStatus = status;
        }
    };

    public void testWriteSequence() throws jmri.ProgrammerException {
        TrafficControllerScaffold tc = new TrafficControllerScaffold();
        CbusProgrammer p = new CbusProgrammer(3, tc);
        
        reply = false;
        rcvdValue = -2;
        rcvdStatus = -2;
        
        p.writeCV(4, 5, testListener);
        
        Assert.assertEquals("listeners", 0, tc.numListeners());
        Assert.assertEquals("sent count", 1, tc.outbound.size());
        Assert.assertEquals("content 1", "96 00 03 04 05", 
                                tc.outbound.get(0).toString());

        // no reply from CAN and listener replies immediately,
        // contrast read test below
        
        Assert.assertTrue("listener invoked", reply);
        Assert.assertEquals("status", 0, rcvdStatus);
    }

    public void testReadSequence() throws jmri.ProgrammerException {
        TrafficControllerScaffold tc = new TrafficControllerScaffold();
        CbusProgrammer p = new CbusProgrammer(3, tc);
        
        reply = false;
        rcvdValue = -2;
        rcvdStatus = -2;
        
        p.readCV(4, testListener);
        
        Assert.assertEquals("listeners", 0, tc.numListeners());
        Assert.assertEquals("sent count", 1, tc.outbound.size());
        Assert.assertEquals("content 1", "71 00 03 04",
                                tc.outbound.get(0).toString());
        Assert.assertTrue("listener not invoked", !reply);

        // pretend reply from CAN

        int[] frame = new int[]{0x97, 0, 3, 5};
        CanReply f = new CanReply(frame);
        p.reply(f);
        
        Assert.assertTrue("listener invoked", reply);
        Assert.assertEquals("status", 0, rcvdStatus);
        Assert.assertEquals("value", 5, rcvdValue);
    }

    // from here down is testing infrastructure

    public CbusProgrammerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
    	String[] testCaseName = {CbusProgrammerTest.class.getName()};
    	junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(CbusProgrammerTest.class);
        return suite;
    }

    static Logger log = Logger.getLogger(CbusProgrammerTest.class.getName());
    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

}
