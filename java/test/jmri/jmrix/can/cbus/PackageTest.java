package jmri.jmrix.can.cbus;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        jmri.jmrix.can.cbus.configurexml.PackageTest.class,
        jmri.jmrix.can.cbus.eventtable.PackageTest.class,
        jmri.jmrix.can.cbus.node.PackageTest.class,
        jmri.jmrix.can.cbus.simulator.PackageTest.class,
        jmri.jmrix.can.cbus.swing.PackageTest.class,
        BundleTest.class,
        CbusAddressTest.class,
        CbusCommandStationTest.class,
        CbusConfigurationManagerTest.class,
        CbusConstantsTest.class,
        CbusDccProgrammerManagerTest.class,
        CbusDccOpsModeProgrammerTest.class,
        CbusDccProgrammerTest.class,
        CbusEventHighlighterTest.class,
        CbusEventTest.class,
        CbusFilterTest.class,
        CbusLightManagerTest.class,
        CbusLightTest.class,
        CbusMessageTest.class,
        CbusNameServiceTest.class,
        CbusOpCodesTest.class,
        CbusPowerManagerTest.class,
        CbusPreferencesTest.class,
        CbusProgrammerTest.class,
        CbusProgrammerManagerTest.class,
        CbusReporterTest.class,
        CbusReporterManagerTest.class,
        CbusSendTest.class,
        CbusSensorTest.class,
        CbusSensorManagerTest.class,
        CbusThrottleTest.class,
        CbusThrottleManagerTest.class,
        CbusTurnoutManagerTest.class,
        CbusTurnoutTest.class
})

/**
 * Tests for the jmri.jmrix.can.cbus package.
 *
 * @author Bob Jacobsen Copyright 2008
 */
public class PackageTest  {
}
