package jmri.jmrix.roco;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        RocoConnectionTypeListTest.class,
        jmri.jmrix.roco.z21.PackageTest.class,
        BundleTest.class,
        RocoCommandStationTest.class,
})

/**
 * Tests for the jmri.jmrix.roco package
 *
 * @author	Bob Jacobsen
 */
public class PackageTest  {
}
