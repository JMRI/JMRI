package jmri;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;
import org.junit.AfterClass;
import org.junit.BeforeClass;

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
                 features="java/acceptancetest/features/web",
                 tags = {"not @webtest", "not @Ignore", "not @ignore"},
                 glue = {"jmri"} )
public class RunCucumberTest {
   
   @BeforeClass
   public static void beforeTests(){
     jmri.util.JUnitUtil.setUp();
   }

   @AfterClass
   public static void afterTests(){
      jmri.util.web.BrowserFactory.CloseAllDriver();
      jmri.util.JUnitUtil.tearDown();
   }

}
