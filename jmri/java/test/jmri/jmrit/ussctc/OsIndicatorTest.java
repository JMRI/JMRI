// OsIndicatorTest.java

package jmri.jmrit.ussctc;

import org.apache.log4j.Logger;
import junit.framework.*;

/**
 * Tests for classes in the jmri.jmrit.ussctc.OsIndicator class
 * @author	Bob Jacobsen  Copyright 2003, 2007
 * @version	$Revision$
 */
public class OsIndicatorTest extends TestCase {

    public void testFrameCreate(){
        new OsIndicator("12", "34", "56");
    }

    // from here down is testing infrastructure

    public OsIndicatorTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {OsIndicatorTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(OsIndicatorTest.class);
        return suite;
    }

    static Logger log = Logger.getLogger(OsIndicatorTest.class.getName());

}
