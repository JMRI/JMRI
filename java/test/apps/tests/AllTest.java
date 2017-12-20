package apps.tests;

import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Invoke all the JMRI project JUnit tests via a GUI interface.
 *
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author	Bob Jacobsen
 */
public class AllTest extends TestCase {

    public AllTest(String s) {
        super(s);
    }

    // note that initLogging has to be invoked _twice_ to get logging to
    // work in both the test routines themselves, and also in the TestSuite
    // code.  And even though it should be protected by a static, it runs
    // twice!  There are probably two sets of classes being created here...
    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", AllTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite
    public static Test suite() {
        // all tests from here down in heirarchy
        TestSuite suite = new TestSuite("AllTest");  // no tests in this class itself
        // all tests from other classes
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(apps.PackageTest.class));
        // at the end, we check for Log4J messages again
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.util.Log4JErrorIsErrorTest.class));
        return suite;
    }

    public static void initLogging() {
        apps.tests.Log4JFixture.initLogging();
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        JUnitUtil.setUp();
    }

    @Override
    protected void tearDown() {
        JUnitUtil.tearDown();
    }

}
