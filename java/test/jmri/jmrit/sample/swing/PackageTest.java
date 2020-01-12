package jmri.jmrit.sample.swing;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    SampleConfigPaneTest.class,
    SampleConfigStartUpActionFactoryTest.class
})

/**
 * Invokes complete set of tests in the jmri.jmrit tree
 *
 * @author	Bob Jacobsen Copyright 2018
 */
public class PackageTest {
}

