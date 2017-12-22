package jmri.util.exceptionhandler;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        BundleTest.class,
        UncaughtExceptionHandlerTest.class,
})

/**
 * Invokes complete set of tests in the jmri.util.swing tree
 *
 * @author	Bob Jacobsen Copyright 2003
 */
public class PackageTest  {
}
