// OBlockTest.java
package jmri.jmrit.logix;

import jmri.Block;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author	Bob Jacobsen Copyright 2010, 2014
 */
public class OBlockTest extends TestCase {

    @SuppressWarnings("all") // otherwise, you get "Comparing identical" warning (until something breaks!)
    public void testEqualCoding() {
        // the following match is required by the JavaDoc
        Assert.assertTrue("Block.UNKNOWN == OBlock.DARK", Block.UNKNOWN == OBlock.DARK);
    }

    public void testSeparateCoding() {
        Assert.assertTrue("Block.OCCUPIED != OBlock.ALLOCATED", Block.OCCUPIED != OBlock.ALLOCATED);
        Assert.assertTrue("Block.OCCUPIED != OBlock.RUNNING", Block.OCCUPIED != OBlock.RUNNING);
        Assert.assertTrue("Block.OCCUPIED != OBlock.OUT_OF_SERVICE", Block.OCCUPIED != OBlock.OUT_OF_SERVICE);
        Assert.assertTrue("Block.OCCUPIED != OBlock.TRACK_ERROR", Block.OCCUPIED != OBlock.TRACK_ERROR);
        Assert.assertTrue("Block.OCCUPIED != OBlock.UNOCCUPIED", Block.OCCUPIED != OBlock.UNOCCUPIED);

        Assert.assertTrue("Block.UNOCCUPIED != OBlock.ALLOCATED", Block.UNOCCUPIED != OBlock.ALLOCATED);
        Assert.assertTrue("Block.UNOCCUPIED != OBlock.RUNNING", Block.UNOCCUPIED != OBlock.RUNNING);
        Assert.assertTrue("Block.UNOCCUPIED != OBlock.OUT_OF_SERVICE", Block.UNOCCUPIED != OBlock.OUT_OF_SERVICE);
        Assert.assertTrue("Block.UNOCCUPIED != OBlock.TRACK_ERROR", Block.UNOCCUPIED != OBlock.TRACK_ERROR);

        Assert.assertTrue("Block.UNDETECTED != OBlock.ALLOCATED", Block.UNDETECTED != OBlock.ALLOCATED);
        Assert.assertTrue("Block.UNDETECTED != OBlock.RUNNING", Block.UNDETECTED != OBlock.RUNNING);
        Assert.assertTrue("Block.UNDETECTED != OBlock.OUT_OF_SERVICE", Block.UNDETECTED != OBlock.OUT_OF_SERVICE);
        Assert.assertTrue("Block.UNDETECTED != OBlock.TRACK_ERROR", Block.UNDETECTED != OBlock.TRACK_ERROR);
        Assert.assertTrue("Block.UNDETECTED != OBlock.UNOCCUPIED", Block.UNDETECTED != OBlock.UNOCCUPIED);

        Assert.assertTrue("Block.UNKNOWN != OBlock.ALLOCATED", Block.UNKNOWN != OBlock.ALLOCATED);
        Assert.assertTrue("Block.UNKNOWN != OBlock.RUNNING", Block.UNKNOWN != OBlock.RUNNING);
        Assert.assertTrue("Block.UNKNOWN != OBlock.OUT_OF_SERVICE", Block.UNKNOWN != OBlock.OUT_OF_SERVICE);
        Assert.assertTrue("Block.UNKNOWN != OBlock.TRACK_ERROR", Block.UNKNOWN != OBlock.TRACK_ERROR);
        Assert.assertTrue("Block.UNKNOWN != OBlock.UNOCCUPIED", Block.UNKNOWN != OBlock.UNOCCUPIED);

    }

    // from here down is testing infrastructure
    public OBlockTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", OBlockTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        return new TestSuite(OBlockTest.class);
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
