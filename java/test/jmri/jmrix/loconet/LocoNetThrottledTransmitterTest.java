package jmri.jmrix.loconet;

import java.util.concurrent.TimeUnit;
import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * Tests for the jmri.jmrix.loconet.LocoNetThrottledTransmitter class.
 *
 * @author Bob Jacobsen Copyright 2001, 2002, 2009, 2015
 */
public class LocoNetThrottledTransmitterTest extends TestCase {

    public void testCtorAndDispose() {
        LocoNetThrottledTransmitter q = new LocoNetThrottledTransmitter(null, false);
        q.dispose();
        JUnitUtil.waitFor(()->{return !q.running;}, "stopped");
    }

    public void testMemoCtor() {
        LocoNetThrottledTransmitter q = new LocoNetThrottledTransmitter(null, false);
        new LocoNetThrottledTransmitter.Memo(null, 100, TimeUnit.MILLISECONDS);

        q.dispose();
        JUnitUtil.waitFor(()->{return !q.running;}, "stopped");
    }

    public void testMemoComparable() {
        LocoNetThrottledTransmitter.Memo m50   = new LocoNetThrottledTransmitter.Memo(null, 50, TimeUnit.MILLISECONDS);
        LocoNetThrottledTransmitter.Memo m100a = new LocoNetThrottledTransmitter.Memo(null, 100, TimeUnit.MILLISECONDS);
        LocoNetThrottledTransmitter.Memo m100b = new LocoNetThrottledTransmitter.Memo(null, 100, TimeUnit.MILLISECONDS);
        LocoNetThrottledTransmitter.Memo m200a = new LocoNetThrottledTransmitter.Memo(null, 200, TimeUnit.MILLISECONDS);
        LocoNetThrottledTransmitter.Memo m200b = new LocoNetThrottledTransmitter.Memo(null, 200, TimeUnit.MILLISECONDS);

        Assert.assertNotNull("exists", m100b);
        Assert.assertNotNull("exists", m200b);
        Assert.assertEquals("same object", 0, m100a.compareTo(m100a));
        Assert.assertEquals("same object", 0, m100a.compareTo(m100a));

        Assert.assertEquals("less than 1", -1, m100a.compareTo(m200a));
        Assert.assertEquals("less than 2", -1, m50.compareTo(m100a));
        Assert.assertEquals("less than 3", -1, m50.compareTo(m200a));

        Assert.assertEquals("greater than 1", 1, m200a.compareTo(m100a));
        Assert.assertEquals("greater than 2", 1, m100a.compareTo(m50));
        Assert.assertEquals("greater than 3", 1, m200a.compareTo(m50));
    }

    public void testThreadStartStop() {
        LocoNetThrottledTransmitter q = new LocoNetThrottledTransmitter(null, false);
        JUnitUtil.waitFor(()->{return q.running;}, "started");

        Assert.assertTrue("started", q.running);

        q.dispose();
        JUnitUtil.waitFor(()->{return !q.running;}, "stopped");
    }

    public void testSendOneImmediate() {
        LocoNetInterfaceScaffold s = new LocoNetInterfaceScaffold();
        LocoNetThrottledTransmitter q = new LocoNetThrottledTransmitter(s, false);

        LocoNetMessage m1;

        m1 = new LocoNetMessage(1);
        m1.setElement(0, 0x01);  // dummy value

        q.minInterval = 0;
        q.sendLocoNetMessage(m1);

        JUnitUtil.waitFor(()->{return s.outbound.size() == 1;}, "one sent");

        Assert.assertEquals("one sent", 1, s.outbound.size());
        Assert.assertEquals("right one", m1, s.outbound.elementAt(s.outbound.size() - 1));

        q.dispose();
        JUnitUtil.waitFor(()->{return !q.running;}, "stopped");
    }

    public void testSendOneNowOneLater() {
        LocoNetInterfaceScaffold s = new LocoNetInterfaceScaffold();
        LocoNetThrottledTransmitter q = new LocoNetThrottledTransmitter(s, false);

        LocoNetMessage m1 = new LocoNetMessage(1);
        m1.setElement(0, 0x01);  // dummy value
        LocoNetMessage m2 = new LocoNetMessage(1);
        m2.setElement(0, 0x02);  // dummy value

        q.minInterval = 1;
        q.sendLocoNetMessage(m1);
        q.minInterval = 100;
        q.sendLocoNetMessage(m2);

        JUnitUtil.waitFor(()->{return s.outbound.size() == 1;}, "only one sent failed with s.outbound.size() "+s.outbound.size());

        Assert.assertEquals("only one sent", 1, s.outbound.size());
        Assert.assertEquals("right one", m1, s.outbound.elementAt(0));

        JUnitUtil.waitFor(()->{return s.outbound.size() == 2;}, "only two sent failed with s.outbound.size() "+s.outbound.size());

        Assert.assertEquals("two sent", 2, s.outbound.size());
        Assert.assertEquals("right 2nd", m2, s.outbound.elementAt(1));

        q.dispose();
        JUnitUtil.waitFor(()->{return !q.running;}, "stopped");
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

        JUnitUtil.waitFor(()->{return s.outbound.size() == 1;}, "only one sent");

        Assert.assertEquals("only one sent", 1, s.outbound.size());
        Assert.assertEquals("right one", m1, s.outbound.elementAt(0));

        q.sendLocoNetMessage(m2);
        JUnitUtil.waitFor(()->{return s.outbound.size() == 2;}, "two sent");

        Assert.assertEquals("two sent", 2, s.outbound.size());
        Assert.assertEquals("right 2nd", m2, s.outbound.elementAt(1));

        q.dispose();
        JUnitUtil.waitFor(()->{return !q.running;}, "stopped");
    }

    // from here down is testing infrastructure
    public LocoNetThrottledTransmitterTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {LocoNetThrottledTransmitterTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(LocoNetThrottledTransmitterTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        JUnitUtil.setUp();
    }

    @Override
    protected void tearDown() {
        JUnitUtil.tearDown();
    }

}
