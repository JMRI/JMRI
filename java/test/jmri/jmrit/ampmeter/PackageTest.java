package jmri.jmrit.ampmeter;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        BundleTest.class,
        AmpMeterActionTest.class,
        AmpMeterFrameTest.class,
})

/**
 * Invokes complete set of tests in the jmri.jmrit.ampmeter tree
 *
 * @author	Bob Jacobsen Copyright 2001, 2003, 2012
 */
public class PackageTest {

}
