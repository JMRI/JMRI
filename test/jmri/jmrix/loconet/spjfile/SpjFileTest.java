// SpjFileTest.java

package jmri.jmrix.loconet.spjfile;

import apps.tests.*;
import junit.framework.*;

/**
 * Tests for the jmri.jmrix.loconet.spjfile package
 * @author	Bob Jacobsen Copyright (C) 2006
 * @version     $Revision: 1.2 $
 */
public class SpjFileTest extends TestCase {

    public void testCreate() {
        new SpjFile("ac4400.spj");
    }

    public void testRead() throws java.io.IOException {
        new SpjFile("java/test/jmri/jmrix/loconet/spjfile/sd38_2.spj").read();
    }

    // from here down is testing infrastructure

    public SpjFileTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SpjFileTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SpjFileTest.class);
        return suite;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SpjFileTest.class.getName());

}
