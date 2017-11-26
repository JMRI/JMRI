package jmri.jmrit.conditional;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
      BundleTest.class,
      ConditionalEditBaseTest.class,
      ConditionalListEditTest.class,
      ConditionalTreeEditTest.class
})
/**
 * Invokes complete set of tests in the jmri.jmrit.conditional tree
 *
 * @author  Dave Sand Copyright (C) 2017
 */
public class PackageTest {
}
