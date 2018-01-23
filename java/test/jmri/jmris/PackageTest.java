//JmrisTest.java
package jmri.jmris;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        jmri.jmris.srcp.SRCPTest.class,
        jmri.jmris.simpleserver.PackageTest.class,
        jmri.jmris.json.PackageTest.class,
        jmri.jmris.JmriServerTest.class,
        jmri.jmris.JmriConnectionTest.class,
        jmri.jmris.ServiceHandlerTest.class,
        BundleTest.class,
        JmriServerFrameTest.class,
        JmriServerActionTest.class,
        ServerMenuTest.class,
        AbstractRouteServerTest.class,
        AbstractSignalMastServerTest.class,
})

/**
 * Set of tests for the jmri.jmris package
 *
 * @author	Paul Bender Copyright 2010
 */
public class PackageTest {
}
