package jmri.jmrix.openlcb.swing;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        jmri.jmrix.openlcb.swing.hub.PackageTest.class,
        jmri.jmrix.openlcb.swing.tie.PackageTest.class,
        jmri.jmrix.openlcb.swing.networktree.PackageDemo.class,
        jmri.jmrix.openlcb.swing.monitor.PackageTest.class,
        jmri.jmrix.openlcb.swing.clockmon.PackageTest.class,
        jmri.jmrix.openlcb.swing.downloader.PackageTest.class,
        jmri.jmrix.openlcb.swing.send.PackageTest.class,
        BundleTest.class
})

/**
 * Tests for the jmri.jmrix.openlcb package.
 *
 * @author Bob Jacobsen Copyright 2009, 2012
 */
public class PackageTest {

}
