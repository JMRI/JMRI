// OsIndicatorActionTest.java

package jmri.jmrit.ussctc;

import org.apache.log4j.Logger;
import junit.framework.*;

/**
 * Tests for classes in the jmri.jmrit.ussctc.OsIndicatorAction class
 * @author	Bob Jacobsen  Copyright 2003, 2007, 2010
 * @version	$Revision$
 */
public class OsIndicatorActionTest extends jmri.util.SwingTestCase {

    public void testFrameCreate() {
        new OsIndicatorAction("test");
    }

    public void testActionCreateAndFire() {
        new OsIndicatorAction("test").actionPerformed(null);
    }


    // from here down is testing infrastructure

    public OsIndicatorActionTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {OsIndicatorActionTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(OsIndicatorActionTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() throws Exception { 
        apps.tests.Log4JFixture.setUp(); 
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
    }
    protected void tearDown() throws Exception { 
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown(); 
    }
    
    static Logger log = Logger.getLogger(OsIndicatorActionTest.class.getName());

}
