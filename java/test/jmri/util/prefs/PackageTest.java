package jmri.util.prefs;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
    JmriConfigurationProviderTest.class,
    JmriPreferencesProviderTest.class,
    InitializationExceptionTest.class,
    HasConnectionButUnableToConnectExceptionTest.class,
    JmriUserInterfaceConfigurationProviderTest.class
})
/**
 * Invokes complete set of tests in the jmri.util tree
 *
 * @author	Bob Jacobsen Copyright 2003
 */
public class PackageTest {
}
