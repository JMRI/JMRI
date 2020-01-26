package apps.tests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.junit.runner.RunWith;

import jmri.util.junit.TestClassMainMethod;

/**
 * Invoke all the JMRI project JUnit tests via a GUI interface.
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
 * @author Bob Jacobsen
 */

@RunWith(JUnitPlatform.class)
@SuiteDisplayName("AllTest")
@SelectPackages({"jmri","apps"})
@ExcludeClassNamePatterns({"HeadLessTest","*PackageTest*"})
public class AllTest {

    @Deprecated // 4.13.3  No longer needed so long as there's a call to jmri.util.JUnitUtil.setup() in the usual way
    public static void initLogging() {
    }

    static public void main(String[] args) {
        TestClassMainMethod.run(AllTest.class);
    }
}
