// OlcbThrottleTest.java

package jmri.jmrix.openlcb;

import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.TestTrafficController;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.openlcb.OlcbThrottle class.
 *
 * @author	Bob Jacobsen Copyright 2008, 2010, 2011
 * @version     $Revision$
 */
public class OlcbThrottleTest extends TestCase {

    public void testCtor() {
    }
    
    // from here down is testing infrastructure

    public OlcbThrottleTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
    	String[] testCaseName = {OlcbThrottleTest.class.getName()};
    	junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(OlcbThrottleTest.class);
        return suite;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(OlcbThrottleTest.class.getName());
    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
}
