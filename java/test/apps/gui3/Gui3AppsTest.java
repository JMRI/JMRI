// Gui3AppsTest.java
package apps.gui3;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for GUI3 base class.
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
 * @author	Bob Jacobsen Copyright 2009
 * @version $Revision$
 */
public class Gui3AppsTest extends TestCase {

    Gui3AppsTest(String s) {
        super(s);
    }

    // note that initLogging has to be invoked _twice_ to get logging to
    // work in both the test routines themselves, and also in the TestSuite
    // code.  And even though it should be protected by a static, it runs
    // twice!  There are probably two sets of classes being created here...
    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", Gui3AppsTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite
    public static Test suite() {
        // all tests from here down in heirarchy
        TestSuite suite = new TestSuite("Gui3AppsTest");  // no tests in this class itself
        // all tests from other classes
        //suite.addTest(jmri.JmriTest.suite());

        return suite;
    }

    public static void initLogging() {
        apps.tests.Log4JFixture.initLogging();
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
