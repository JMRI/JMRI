package jmri.jmrix.maple;

import jmri.jmrix.AbstractMRMessage;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * JUnit tests for the SerialNode class
 *
 * @author Bob Jacobsen Copyright 2003
 * @author Dave Duchamp multi-node extensions 2003
 */
public class SerialNodeTest {

    private SerialNode b = null;
    private SerialTrafficController tc = null;

    @Test
    public void testConstructor1() {
        Assert.assertEquals("check default ctor address", 1, b.getNodeAddress());
    }

    @Test
    public void testConstructor2() {
        SerialNode c = new SerialNode(3, 0, tc);
        Assert.assertEquals("check ctor address", 3, c.getNodeAddress());
    }

    @Test
    public void testConstructor3() {
        SerialNode d = new SerialNode(4, 0, tc);
        Assert.assertEquals("check ctor address", 4, d.getNodeAddress());
    }

    @Test
    public void testAccessors() {
        SerialNode n = new SerialNode(2, 0, tc);
        n.setNodeAddress(7);
        Assert.assertEquals("check address", 7, n.getNodeAddress());
    }

    @Test
    public void testInitialization1() {
        // no initialization needed for Maple
        AbstractMRMessage m = b.createInitPacket();
        Assert.assertEquals("null message", null, m);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        tc = new SerialTrafficControlScaffold();
        b = new SerialNode(tc);
    }

    @AfterEach
    public void tearDown() {
        b = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

}
