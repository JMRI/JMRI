package jmri;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import jmri.util.junit.TestClassMainMethod;

/**
 * Invoke complete set of tests for the jmri package
 *
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * @author Bob Jacobsen, Copyright (C) 2001, 2002, 2007
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        jmri.PackageTest.class,
        apps.PackageTest.class,
        // at the end, we check for logging messages again
        jmri.util.Log4JErrorIsErrorTest.class
})
public class HeadLessTest {

    // Main entry point
    static public void main(String[] args) {
        // force headless operation
        System.setProperty("java.awt.headless", "true");

        // start tests
        TestClassMainMethod.run(HeadLessTest.class);
    }
}
