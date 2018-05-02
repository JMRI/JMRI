package jmri.jmrix.maple;

import jmri.jmrix.AbstractMRMessage;
import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * JUnit tests for the SerialNode class
 *
 * @author	Bob Jacobsen Copyright 2003
 * @author	Dave Duchamp multi-node extensions 2003
 */
public class SerialNodeTest extends TestCase {

    private SerialNode b = null;
    private SerialTrafficController tc = null;

    public void testConstructor1() {
        Assert.assertEquals("check default ctor address", 1, b.getNodeAddress());
    }

    public void testConstructor2() {
        SerialNode c = new SerialNode(3, 0,tc);
        Assert.assertEquals("check ctor address", 3, c.getNodeAddress());
    }

    public void testConstructor3() {
        SerialNode d = new SerialNode(4, 0,tc);
        Assert.assertEquals("check ctor address", 4, d.getNodeAddress());
    }

    public void testAccessors() {
        SerialNode n = new SerialNode(2, 0,tc);
        n.setNodeAddress(7);
        Assert.assertEquals("check address", 7, n.getNodeAddress());
    }

    public void testInitialization1() {
        // no initialization needed for Maple
        AbstractMRMessage m = b.createInitPacket();
        Assert.assertEquals("null message", null, m);
    }

    // from here down is testing infrastructure
    public SerialNodeTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", SerialNodeTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SerialNodeTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        JUnitUtil.setUp();
        tc = new SerialTrafficControlScaffold();
        b = new SerialNode(tc);
    }

    @Override
    protected void tearDown() {
        b = null;
        JUnitUtil.tearDown();
    }

}
