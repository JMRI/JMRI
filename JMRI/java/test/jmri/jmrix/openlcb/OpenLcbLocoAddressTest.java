package jmri.jmrix.openlcb;

import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;
import org.openlcb.NodeID;

/**
 * Tests for the jmri.jmrix.openlcb.OpenLcbLocoAddress class.
 *
 * @author	Bob Jacobsen Copyright 2008, 2010, 2011
 */
public class OpenLcbLocoAddressTest extends TestCase {

    public void testEqualsNull() {
        OpenLcbLocoAddress a = new OpenLcbLocoAddress(new NodeID(new byte[]{1, 2, 3, 4, 5, 6}));
        Assert.assertTrue(!a.equals(null));
    }

    public void testEquals() {
        OpenLcbLocoAddress a = new OpenLcbLocoAddress(new NodeID(new byte[]{1, 2, 3, 4, 5, 6}));
        Assert.assertTrue(a.equals(new OpenLcbLocoAddress(new NodeID(new byte[]{1, 2, 3, 4, 5, 6}))));
    }

    public void testNotEqualsDifferentNode() {
        OpenLcbLocoAddress a = new OpenLcbLocoAddress(new NodeID(new byte[]{1, 2, 3, 4, 5, 6}));
        Assert.assertTrue(!a.equals(new OpenLcbLocoAddress(new NodeID(new byte[]{1, 2, 3, 4, 0, 0}))));
    }

    public void testEqualsWrongType() {
        OpenLcbLocoAddress a = new OpenLcbLocoAddress(new NodeID(new byte[]{1, 2, 3, 4, 5, 6}));
        Assert.assertTrue(!a.equals("foo"));
        Assert.assertTrue(!"foo".equals(a));
    }

    // from here down is testing infrastructure
    public OpenLcbLocoAddressTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {OpenLcbLocoAddressTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(OpenLcbLocoAddressTest.class);
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
