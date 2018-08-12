package jmri.jmrix.loconet.hexfile;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        BundleTest.class,
        jmri.jmrix.loconet.hexfile.configurexml.PackageTest.class,
        HexFileFrameTest.class,
        HexFileServerTest.class,
        LnHexFilePortTest.class,
        LocoNetSystemConnectionMemoTest.class,
        LnHexFileActionTest.class,
        LnSensorManagerTest.class,
})

/**
 * Tests for the jmri.jmrix.loconet.hexfile package.
 *
 * @author	Bob Jacobsen Copyright 2001, 2003, 2006, 2008
 */
public class PackageTest  {
}
