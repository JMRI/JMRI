// OPathTest.java
package jmri.jmrit.logix;

import jmri.Block;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the OPath class
 *
 * @author	Bob Jacobsen Copyright 2010
 */
public class OPathTest extends TestCase {

    public void testCtor() {
        Block b = new Block("IB1");

        OPath op = new OPath(b, "name");

        Assert.assertEquals("name", "name", op.getName());
        Assert.assertEquals("block", b, op.getBlock());
    }

    public void testNullBlockCtor() {

        OPath op = new OPath(null, "name");

        Assert.assertEquals("name", "name", op.getName());
        Assert.assertEquals("block", null, op.getBlock());
    }

    public void testSetBlockNonNull() {
        Block b1 = new Block("IB1");
        Block b2 = new Block("IB2");

        OPath op = new OPath(b1, "name");
        op.setBlock(b2);

        Assert.assertEquals("block", b2, op.getBlock());
    }

    public void testSetBlockWasNull() {
        Block b = new Block("IB1");

        OPath op = new OPath(null, "name");
        op.setBlock(b);

        Assert.assertEquals("block", b, op.getBlock());
    }

    public void testSetBlockToNull() {
        Block b1 = new Block("IB1");

        OPath op = new OPath(b1, "name");
        op.setBlock(null);

        Assert.assertEquals("block", null, op.getBlock());
    }

    // from here down is testing infrastructure
    public OPathTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", OPathTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        return new TestSuite(OPathTest.class);
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
