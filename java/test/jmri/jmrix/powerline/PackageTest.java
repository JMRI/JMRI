package jmri.jmrix.powerline;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        X10SequenceTest.class,
        SerialTurnoutTest.class,
        SerialTurnoutManagerTest.class,
        SerialSensorManagerTest.class,
        SerialNodeTest.class,
        SerialAddressTest.class,
        jmri.jmrix.powerline.cm11.PackageTest.class,
        jmri.jmrix.powerline.insteon2412s.PackageTest.class,
        jmri.jmrix.powerline.simulator.PackageTest.class,
        jmri.jmrix.powerline.cp290.PackageTest.class,
        jmri.jmrix.powerline.serialdriver.PackageTest.class,
        jmri.jmrix.powerline.configurexml.PackageTest.class,
        jmri.jmrix.powerline.swing.PackageTest.class,
        SerialSystemConnectionMemoTest.class,
        SerialPortControllerTest.class,
        SerialTrafficControllerTest.class,
        InsteonSequenceTest.class,
        SerialConnectionTypeListTest.class,
        SerialSensorTest.class,
        SerialX10LightTest.class,
        SystemMenuTest.class,
        BundleTest.class,
})

/**
 * Tests for the jmri.jmrix.powerline package.
 *
 * @author Bob Jacobsen Copyright 2003, 2007, 2008
 */
public class PackageTest  {
}
