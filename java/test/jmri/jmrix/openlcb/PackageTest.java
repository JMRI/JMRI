package jmri.jmrix.openlcb;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        CanConverterTest.class,
        OlcbAddressTest.class,
        OpenLcbLocoAddressTest.class,
        OlcbClockControlTest.class,
        OlcbLightManagerTest.class,
        OlcbSensorManagerTest.class,
        OlcbSignalMastTest.class,
        OlcbProgrammerTest.class,
        OlcbProgrammerManagerTest.class,
        OlcbLightTest.class,
        OlcbSensorTest.class,
        OlcbSystemConnectionMemoTest.class,
        OlcbTurnoutManagerTest.class,
        OlcbTurnoutTest.class,
        OlcbTurnoutInheritedTest.class,
        OlcbThrottleTest.class,
        OlcbThrottleManagerTest.class,
        BundleTest.class,
        jmri.jmrix.openlcb.swing.PackageTest.class,
        jmri.jmrix.openlcb.configurexml.PackageTest.class,
        LinkNodeInventoryTest.class,
        OlcbConnectionTypeListTest.class,
        OlcbConstantsTest.class,
        OlcbConfigurationManagerTest.class,
        OpenLcbMenuTest.class,
        OlcbUtilsTest.class,
})


/**
 * Tests for the jmri.jmrix.openlcb package.
 *
 * @author Bob Jacobsen Copyright 2009, 2012, 2015
 */
public class PackageTest {
}
