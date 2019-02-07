package jmri.util.junit.rules;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        RetryRuleTest.class
})

/**
 * Invokes complete set of tests in the jmri.util.junit.rules tree
 *
 * @author	Bob Jacobsen Copyright 2018
 */
public class PackageTest  {
}
