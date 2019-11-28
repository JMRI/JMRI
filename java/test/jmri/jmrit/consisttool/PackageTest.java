package jmri.jmrit.consisttool;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   ConsistFileTest.class,
   ConsistToolActionTest.class,
   ConsistDataModelTest.class,
   ConsistToolFrameTest.class,
   ConsistToolPrefsPanelTest.class,
   BundleTest.class
})

/**
 * Invokes complete set of tests in the jmri.jmrit.consisttool tree
 *
 * @author	Paul Bender Copyright (C) 2015,2016
 */
public class PackageTest {
}
