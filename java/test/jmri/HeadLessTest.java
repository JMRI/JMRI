package jmri;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Invoke complete set of tests for the jmri package
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
 * <P>
 * @author	Bob Jacobsen, Copyright (C) 2001, 2002, 2007
 */
public class HeadLessTest extends TestCase {

    // from here down is testing infrastructure
    public HeadLessTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        // force headless operation
        System.setProperty("java.awt.headless", "true");

        // start tests
        String[] testCaseName = {"-noloading", HeadLessTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite("jmri.JmriTest");  // no tests in this class itself

        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(apps.PackageTest.class));
        return suite;
    }

}
