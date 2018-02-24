package jmri.jmrit.mastbuilder;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   MastBuilderTest.class,
   MastBuilderPaneTest.class,
   MastBuilderActionTest.class,
   BundleTest.class
})

/**
 * Invokes complete set of tests in the jmri.jmrit.mastbuilder tree
 *
 * @author	Paul Bender Copyright (C) 2017
 */
public class PackageTest {
}
