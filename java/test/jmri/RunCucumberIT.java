package jmri;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;
import jmri.util.web.BrowserFactory;

import org.junit.runner.RunWith;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Trigger file for Cucumber tests.
 * <p>
 * This file provides default options for cucumber.
 * </p>
 * <p>
 * To override those using maven add -Dcucumber.options="..." to the maven
 * command line
 * </p>
 * <p>
 * To override those in ant, run:<br>
 * JAVA_OPTIONS='-Dcucumber.options="..."' ant target
 * </p>
 *
 * @author Paul Bender Copyright 2017
 */
@RunWith(Cucumber.class)
@CucumberOptions(plugin = {"junit:cucumber-results.xml", "progress", "json:cucumber-results.json"},
        features = "java/acceptancetest/features/web",
        tags = {"not @webtest", "not @Disabled", "not @Ignore", "not @ignore"},
        glue = {"apps"})
@DisabledIfHeadless
public class RunCucumberIT {

    @BeforeClass
    public static void beforeTests() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initZeroConfServiceManager();
    }

    @AfterClass
    public static void afterTests() {
        BrowserFactory.closeAllDrivers();
        assertTrue(JUnitUtil.resetZeroConfServiceManager());
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }

}
