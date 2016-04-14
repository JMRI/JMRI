package jmri.jmrit.ussctc;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for classes in the jmri.jmrit.ussctc.FollowerAction class
 *
 * @author	Bob Jacobsen Copyright 2003, 2007
 * @version	$Revision$
 */
public class FollowerActionTest extends TestCase {

    public void testFrameCreate() {
        new FollowerAction("test");
    }

    public void testActionCreateAndFire() {
        new FollowerAction("test").actionPerformed(null);
    }

    // from here down is testing infrastructure
    public FollowerActionTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {FollowerActionTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(FollowerActionTest.class);
        return suite;
    }

}
