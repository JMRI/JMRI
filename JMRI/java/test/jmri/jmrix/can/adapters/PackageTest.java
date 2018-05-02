package jmri.jmrix.can.adapters;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        jmri.jmrix.can.adapters.lawicell.PackageTest.class,
        jmri.jmrix.can.adapters.gridconnect.PackageTest.class,
        jmri.jmrix.can.adapters.loopback.PackageTest.class,
})

/**
 * Tests for the jmri.jmrix.can.adapters package.
 *
 * @author Bob Jacobsen Copyright 2009
 */
public class PackageTest  {
}
