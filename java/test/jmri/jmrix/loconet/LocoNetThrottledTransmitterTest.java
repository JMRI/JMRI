// LocoNetThrottledTransmitterTest.java

package jmri.jmrix.loconet;

import org.apache.log4j.Logger;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.concurrent.TimeUnit;

/**
 * Tests for the jmri.jmrix.loconet.LocoNetThrottledTransmitter class.
 * 
 * @author Bob Jacobsen Copyright 2001, 2002, 2009
 * @version $Revision$
 */
public class LocoNetThrottledTransmitterTest extends TestCase {

    public void testCtorAndDispose() {
        new LocoNetThrottledTransmitter(null, false).dispose();
    }

    public void testMemoCtor() {
        LocoNetThrottledTransmitter q = new LocoNetThrottledTransmitter(null, false);
        q.new Memo(null, 100, TimeUnit.MILLISECONDS);

        q.dispose();
    }

    public void testMemoComparable() {
        LocoNetThrottledTransmitter q = new LocoNetThrottledTransmitter(null, false) {
            long nowMSec() { return 0; }
        };
        LocoNetThrottledTransmitter.Memo m50 = q.new Memo(null, 50, TimeUnit.MILLISECONDS);
        LocoNetThrottledTransmitter.Memo m100a = q.new Memo(null, 100, TimeUnit.MILLISECONDS);
        LocoNetThrottledTransmitter.Memo m100b = q.new Memo(null, 100, TimeUnit.MILLISECONDS);
        LocoNetThrottledTransmitter.Memo m200a = q.new Memo(null, 200, TimeUnit.MILLISECONDS);
        LocoNetThrottledTransmitter.Memo m200b = q.new Memo(null, 200, TimeUnit.MILLISECONDS);
        
        Assert.assertNotNull("exists", m100b );
        Assert.assertNotNull("exists", m200b );
        Assert.assertEquals("same object", 0, m100a.compareTo(m100a));
        Assert.assertEquals("same object", 0, m100a.compareTo(m100a));

        Assert.assertEquals("less than 1", -1, m100a.compareTo(m200a));
        Assert.assertEquals("less than 2", -1, m50.compareTo(m100a));
        Assert.assertEquals("less than 3", -1, m50.compareTo(m200a));
        
        Assert.assertEquals("greater than 1", 1, m200a.compareTo(m100a));
        Assert.assertEquals("greater than 2", 1, m100a.compareTo(m50));
        Assert.assertEquals("greater than 3", 1, m200a.compareTo(m50));
        
        q.dispose();
    }

    public void testMemoGetDelay() {
        LocoNetThrottledTransmitter q = new LocoNetThrottledTransmitter(null, false) {
            long nowMSec() { return 0; }
        };
        LocoNetThrottledTransmitter.Memo m5000 = q.new Memo(null, 5000, TimeUnit.MILLISECONDS);

        Assert.assertEquals("nanoseconds", 5000000000l, m5000.getDelay(TimeUnit.NANOSECONDS));
        Assert.assertEquals("microseconds", 5000000l, m5000.getDelay(TimeUnit.MICROSECONDS));
        Assert.assertEquals("milliseconds", 5000l, m5000.getDelay(TimeUnit.MILLISECONDS));
        Assert.assertEquals("seconds", 5l, m5000.getDelay(TimeUnit.SECONDS));

        q.dispose();
    }
    
    void releaseFor(int msec) {
        int priority = Thread.currentThread().getPriority();
        try {
            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
            Thread.yield();
            Thread.sleep(msec);
        } catch (InterruptedException e) {
        } finally {
            Thread.currentThread().setPriority(priority);
        }
    }
    
    public void testThreadStartStop() {
        LocoNetThrottledTransmitter q = new LocoNetThrottledTransmitter(null, false);
        releaseFor(100);
            
        Assert.assertTrue("started", q.running);
            
        q.dispose();
        releaseFor(100);

        Assert.assertTrue("stopped", !q.running);

    }
    
    public void testSendOneImmediate() {
        LocoNetInterfaceScaffold s = new LocoNetInterfaceScaffold();
        LocoNetThrottledTransmitter q = new LocoNetThrottledTransmitter(s, false);

        LocoNetMessage m1;
        
        m1 = new LocoNetMessage(1);
        m1.setElement(0, 0x01);  // dummy value
        
        q.minInterval = 0;
        q.sendLocoNetMessage(m1);
        
        releaseFor(10);
        
        Assert.assertEquals("one sent", 1, s.outbound.size());
        Assert.assertEquals("right one", m1, s.outbound.elementAt(s.outbound.size()-1));
    }
    
    public void testSendOneNowOneLater() {
        LocoNetInterfaceScaffold s = new LocoNetInterfaceScaffold();
        LocoNetThrottledTransmitter q = new LocoNetThrottledTransmitter(s, false);

        LocoNetMessage m1 = new LocoNetMessage(1);
        m1.setElement(0, 0x01);  // dummy value
        LocoNetMessage m2 = new LocoNetMessage(1);
        m2.setElement(0, 0x02);  // dummy value
        
        q.minInterval = 100;
        q.sendLocoNetMessage(m1);
        q.sendLocoNetMessage(m2);
        
        releaseFor(10);
        
        Assert.assertEquals("only one sent", 1, s.outbound.size());
        Assert.assertEquals("right one", m1, s.outbound.elementAt(0));

        releaseFor(120);
        
        Assert.assertEquals("two sent", 2, s.outbound.size());
        Assert.assertEquals("right 2nd", m2, s.outbound.elementAt(1));
    }

    
    public void testAfterTimeNewMessageSentImmediately() {
        LocoNetInterfaceScaffold s = new LocoNetInterfaceScaffold();
        LocoNetThrottledTransmitter q = new LocoNetThrottledTransmitter(s, false);

        LocoNetMessage m1 = new LocoNetMessage(1);
        m1.setElement(0, 0x01);  // dummy value
        LocoNetMessage m2 = new LocoNetMessage(1);
        m2.setElement(0, 0x02);  // dummy value
        
        q.minInterval = 100;
        q.sendLocoNetMessage(m1);
        
        releaseFor(5);
        
        Assert.assertEquals("only one sent", 1, s.outbound.size());
        Assert.assertEquals("right one", m1, s.outbound.elementAt(0));

        releaseFor(200);
        
        q.sendLocoNetMessage(m2);
        releaseFor(5);
        
        Assert.assertEquals("two sent", 2, s.outbound.size());
        Assert.assertEquals("right 2nd", m2, s.outbound.elementAt(1));
    }

    
    // from here down is testing infrastructure

    public LocoNetThrottledTransmitterTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
    	String[] testCaseName = {LocoNetThrottledTransmitterTest.class.getName()};
    	junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(LocoNetThrottledTransmitterTest.class);
        return suite;
    }

    static Logger log = Logger.getLogger(LocoNetThrottledTransmitterTest.class.getName());

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

}
