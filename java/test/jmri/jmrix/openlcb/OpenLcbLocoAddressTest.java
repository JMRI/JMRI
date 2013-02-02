// OpenLcbLocoAddressTest.java

package jmri.jmrix.openlcb;

import org.apache.log4j.Logger;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Assert;

import org.openlcb.NodeID;

/**
 * Tests for the jmri.jmrix.openlcb.OpenLcbLocoAddress class.
 *
 * @author	Bob Jacobsen Copyright 2008, 2010, 2011
 * @version     $Revision$
 */
public class OpenLcbLocoAddressTest extends TestCase {

    public void testEqualsNull() {
        OpenLcbLocoAddress a = new OpenLcbLocoAddress(new NodeID(new byte[]{1,2,3,4,5,6}));
        Assert.assertTrue(!a.equals(null));
    }
    
    public void testEquals() {
        OpenLcbLocoAddress a = new OpenLcbLocoAddress(new NodeID(new byte[]{1,2,3,4,5,6}));
        Assert.assertTrue(a.equals(new OpenLcbLocoAddress(new NodeID(new byte[]{1,2,3,4,5,6}))));
    }
    public void testNotEqualsDifferentNode() {
        OpenLcbLocoAddress a = new OpenLcbLocoAddress(new NodeID(new byte[]{1,2,3,4,5,6}));
        Assert.assertTrue(!a.equals(new OpenLcbLocoAddress(new NodeID(new byte[]{1,2,3,4,0,0}))));
    }
    
    public void testEqualsWrongType() {
        OpenLcbLocoAddress a = new OpenLcbLocoAddress(new NodeID(new byte[]{1,2,3,4,5,6}));
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
    	junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(OpenLcbLocoAddressTest.class);
        return suite;
    }

    static Logger log = Logger.getLogger(OpenLcbLocoAddressTest.class.getName());
    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
}
