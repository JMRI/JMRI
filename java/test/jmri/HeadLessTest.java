package jmri;

import static org.junit.platform.engine.discovery.ClassNameFilter.excludeClassNamePatterns;
import static org.junit.platform.engine.discovery.ClassNameFilter.includeClassNamePatterns;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

import java.io.PrintWriter;

import org.junit.platform.launcher.*;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Invoke complete set of tests for the jmri and apps packages.
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
@SuiteDisplayName("Headless Tests")
public class HeadLessTest {

    // Main entry point
    static public void main(String[] args) {
        // force headless operation
        System.setProperty("java.awt.headless", "true");

        // start tests
        run();
        System.exit(0);
    }

    /**
     * Run tests with a compile-selected RunListener.
     *
     */
    public static void run() {
        SummaryGeneratingListener listener = new jmri.util.junit.PrintingTestListener(System.out); // test-by-test output if enabled
        String[] includePatterns = {"Test.*",".*Test","IT.*",".*IT"};
        String[] excludePatterns = {"AllTest","HeadLessTest","ArchitectureTest"};

        run(listener,includePatterns,excludePatterns);
        TestExecutionSummary summary = listener.getSummary();
        PrintWriter p = new PrintWriter(System.out);
        summary.printTo(p);
        summary.printFailuresTo(p);
    }

    /**
     * Run tests with a specified RunListener.
     *
     * @param listener the listener for the tests
     * @param includePatterns include patterns
     * @param excludePatterns exclude patterns
     */
    public static void run(TestExecutionListener listener,String[] includePatterns,String[] excludePatterns) {
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectPackage("jmri"))
                .selectors(selectPackage("apps"))
                .filters(includeClassNamePatterns(includePatterns))
                .filters(excludeClassNamePatterns(excludePatterns))
                .build();
        Launcher launcher = LauncherFactory.create();
        TestPlan testPlan = launcher.discover(request);
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(testPlan);
    }


}
