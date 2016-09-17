package jmri.jmrit.ussctc;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for Follower classes in the jmri.jmrit.ussctc package
 *
 * @author	Bob Jacobsen Copyright 2007
 * @version	$Revision$
 */
public class FollowerTest extends TestCase {

    public void testFrameCreate() {
        new Follower("12", "34", false, "56");
    }

    // from here down is testing infrastructure
    public FollowerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {FollowerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(FollowerTest.class);
        return suite;
    }

}
