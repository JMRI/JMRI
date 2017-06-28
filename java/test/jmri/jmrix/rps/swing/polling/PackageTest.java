package jmri.jmrix.rps.swing.polling;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Tests for the jmri.jmrix.rps.swing.polling package.
 *
 * @author Bob Jacobsen Copyright 2008
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    PollTableActionTest.class,
    BundleTest.class,
    PollTableFrameTest.class,
    PollDataModelTest.class,
    PollTablePaneTest.class
})
public class PackageTest{
}
