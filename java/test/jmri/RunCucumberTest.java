package jmri;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(plugin = {"junit:cucumber-results.xml","progress"},
                 features="java/acceptancetests/features")
public class RunCucumberTest {
}
