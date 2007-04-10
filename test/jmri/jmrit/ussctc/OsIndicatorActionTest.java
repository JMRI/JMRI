// OsIndicatorActionTest.java

package jmri.jmrit.ussctc;

import junit.framework.*;

/**
 * Tests for classes in the jmri.jmrit.ussctc.OsIndicatorAction class
 * @author	Bob Jacobsen  Copyright 2003, 2007
 * @version	$Revision: 1.1 $
 */
public class OsIndicatorActionTest extends TestCase {

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

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(OsIndicatorActionTest.class.getName());

}
