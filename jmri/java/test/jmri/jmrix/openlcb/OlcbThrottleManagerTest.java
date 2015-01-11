// OlcbThrottleManagerTest.java

package jmri.jmrix.openlcb;

import org.apache.log4j.Logger;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.openlcb.OlcbThrottleManager class.
 *
 * @author	Bob Jacobsen Copyright 2008, 2010, 2011
 * @version     $Revision$
 */
public class OlcbThrottleManagerTest extends TestCase {

    public void testCtor() {
    }
    
    // from here down is testing infrastructure

    public OlcbThrottleManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
    	String[] testCaseName = {OlcbThrottleManagerTest.class.getName()};
    	junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(OlcbThrottleManagerTest.class);
        return suite;
    }

    static Logger log = Logger.getLogger(OlcbThrottleManagerTest.class.getName());
    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
}
