package jmri.jmrix.pricom;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        jmri.jmrix.pricom.pockettester.PackageTest.class,
        jmri.jmrix.pricom.downloader.PackageTest.class,
        PricomMenuTest.class,
        BundleTest.class,
})

/**
 * Tests for the jmri.jmrix.pricom package.
 *
 * @author Bob Jacobsen Copyright 2005
 */
public class PackageTest  {
}
