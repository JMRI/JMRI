package jmri.jmrit.sample;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        SampleFunctionalClassTest.class,
        jmri.jmrit.sample.configurexml.PackageTest.class,
        jmri.jmrit.sample.swing.PackageTest.class
})

/**
 * Invokes complete set of tests in the jmri.jmrit tree
 *
 * @author	Bob Jacobsen Copyright 2001, 2003, 2012, 2018
 */
public class PackageTest {
}

