package apps;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;
import org.junit.*;

/**
 * Trigger file for Cucumber tests.
 * <p>
 * This file provides default options for cucumber.</p>
 * <p>
 * To override those using maven add -Dcucumber.options="..." to the maven
 * command line
 * </p>
 * <p>
 * To override those in ant, run:<br/>
 * JAVA_OPTIONS='-Dcucumber.options="..."' ant target
 * </p>
 * @author	Paul Bender Copyright 2017
 */


@RunWith(Cucumber.class)
@CucumberOptions(plugin = {"junit:cucumber-results.xml","progress","json:cucumber-results.json"},
                 features="java/acceptancetest/features/apps",
                 tags = {"not @webtest", "not @Ignore", "not @ignore"},
                 glue = {"apps","jmri"} )
public class RunCucumberTest {

    @BeforeClass
    public static void setUpClass() {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterClass
    public static void tearDownClass() {
        jmri.util.JUnitUtil.tearDown();
    }
}
