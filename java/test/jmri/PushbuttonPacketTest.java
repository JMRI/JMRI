package jmri;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the PushbuttonPacket class
 *
 * @author	Bob Jacobsen Copyright (C) 2010
 */
public class PushbuttonPacketTest extends TestCase {

    public void testImmutableNames() {
        String[] c1 = PushbuttonPacket.getValidDecoderNames();
        String[] c2 = PushbuttonPacket.getValidDecoderNames();
        Assert.assertEquals(c1.length, c2.length);
        Assert.assertTrue(c1.length > 0);
        Assert.assertTrue(c1[0].equals(c2[0]));
        c1[0] = "foo";
        Assert.assertTrue(!c1[0].equals(c2[0]));
    }

    // from here down is testing infrastructure
    public PushbuttonPacketTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {PushbuttonPacketTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(PushbuttonPacketTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
