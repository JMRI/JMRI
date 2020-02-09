package jmri;

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
@RunWith(JUnitPlatform.class)
@SuiteDisplayName("Headless Tests")
@SelectPackages({"jmri","apps"})
@ExcludeClassNamePatterns({"^AllTest$","^FileLineEndingsTest$","ArchitectureTest"})
public class HeadLessTest {

    // Main entry point
    static public void main(String[] args) {
        // force headless operation
        System.setProperty("java.awt.headless", "true");

        // start tests
        run(HeadLessTest.class);
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
