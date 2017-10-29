package jmri.jmrit.log;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    BundleTest.class,
    Log4JTreePaneTest.class,
    LogActionTest.class,
    LogFrameTest.class,
    LogPanelTest.class,
    LogOutputWindowActionTest.class
})
/**
 * Invokes complete set of tests in the jmri.jmrit.log tree
 *
 * @author	Bob Jacobsen Copyright 2003, 2010
 */
public class PackageTest {
}
