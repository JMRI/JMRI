package apps.tests;

import org.junit.internal.TextListener;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.ExcludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

import org.junit.runner.notification.RunListener;

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
@ExcludeClassNamePatterns({"HeadLessTest","FileLineEndingsTest","ArchitectureTest"})
public class AllTest {

    @Deprecated // 4.13.3  No longer needed so long as there's a call to jmri.util.JUnitUtil.setup() in the usual way
    public static void initLogging() {
    }

    static public void main(String[] args) {
        run(AllTest.class);
    }

    /**
     * Run tests with a default RunListener.
     *
     * @param testClass the class containing tests to run
     */
    public static void run(Class<?> testClass){
        run(new TextListener(System.out),testClass);
    }

    /**
     * Run tests with a specified RunListener
     *
     * @param listener the listener for the tests
     * @param testClass the class containing tests to run
     */
    public static void run(RunListener listener, Class<?> testClass) {
        JUnitCore runner = new JUnitCore();
        runner.addListener(listener);
        Result result = runner.run(testClass);
        System.exit(result.wasSuccessful() ? 0 : 1);
    }
}

