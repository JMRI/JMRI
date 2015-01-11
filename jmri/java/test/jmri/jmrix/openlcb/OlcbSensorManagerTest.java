// OlcbSensorManagerTest.java

package jmri.jmrix.openlcb;

import org.apache.log4j.Logger;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.openlcb.OlcbSensorManager class.
 *
 * @author	Bob Jacobsen Copyright 2008, 2010
 * @version     $Revision$
 */
public class OlcbSensorManagerTest extends TestCase {

    public void testDummy() {
    }
        
    // from here down is testing infrastructure

    public OlcbSensorManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
    	String[] testCaseName = {OlcbSensorManagerTest.class.getName()};
    	junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(OlcbSensorManagerTest.class);
        return suite;
    }

    static Logger log = Logger.getLogger(OlcbSensorManagerTest.class.getName());
    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

}
