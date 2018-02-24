/**
 * PackageTest.java
 *
 * Description:	tests for the jmri.implementation.swing package
 *
 * @author	Bob Jacobsen 2009
 */
package jmri.implementation.swing;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        SwingShutDownTaskDemo.class,  // Normally a user-invoked demo, but in this case also a test
        BundleTest.class,
        SwingShutDownTaskTest.class,
})

public class PackageTest {
}
