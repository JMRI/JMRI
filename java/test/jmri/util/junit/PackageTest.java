package jmri.util.junit;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        jmri.util.junit.rules.PackageTest.class,
        jmri.util.junit.annotations.PackageTest.class
})

/**
 * Invokes complete set of tests in the jmri.util.junit tree
 *
 * @author	Bob Jacobsen Copyright 2018
 */
public class PackageTest  {
}
