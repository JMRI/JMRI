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
@CucumberOptions(plugin = {"junit:cucumber-results.xml","progress"},
                 features="java/acceptancetest/features",
                 tags = {"not @webtest"})
public class RunCucumberTest {
   
   @BeforeClass
   public static void beforeTests(){
      apps.tests.Log4JFixture.setUp();
   }

   @AfterClass
   public static void afterTests(){
      apps.tests.Log4JFixture.tearDown();
   }

}
