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
        BundleTest.class,
        SwingShutDownTaskTest.class,
})

public class PackageTest {
}
