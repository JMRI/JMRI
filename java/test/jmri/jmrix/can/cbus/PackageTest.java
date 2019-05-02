package jmri.jmrix.can.cbus;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        jmri.jmrix.can.cbus.CbusAddressTest.class,
        jmri.jmrix.can.cbus.CbusProgrammerTest.class,
        jmri.jmrix.can.cbus.CbusProgrammerManagerTest.class,
        CbusSensorManagerTest.class,
        jmri.jmrix.can.cbus.CbusSensorTest.class,
        jmri.jmrix.can.cbus.configurexml.PackageTest.class,
        jmri.jmrix.can.cbus.swing.PackageTest.class,
        jmri.jmrix.can.cbus.simulator.PackageTest.class,
        jmri.jmrix.can.cbus.node.PackageTest.class,
        CbusReporterManagerTest.class,
        CbusCabSignalTest.class,
        CbusCabSignalManagerTest.class,
        CbusConstantsTest.class,
        CbusEventHighlighterTest.class,
        CbusFilterTest.class,
        CbusMessageTest.class,
        CbusOpCodesTest.class,
        CbusCommandStationTest.class,
        CbusConfigurationManagerTest.class,
        CbusDccProgrammerManagerTest.class,
        CbusDccOpsModeProgrammerTest.class,
        CbusDccProgrammerTest.class,
        CbusLightManagerTest.class,
        CbusLightTest.class,
        CbusMultiMeterTest.class,
        CbusPowerManagerTest.class,
        CbusPreferenceTest.class,
        CbusReporterTest.class,
        CbusThrottleTest.class,
        CbusThrottleManagerTest.class,
        CbusTurnoutManagerTest.class,
        CbusTurnoutTest.class,
        BundleTest.class,
        CbusEventTest.class,
        CbusSendTest.class,
        CbusNameServiceTest.class,
        jmri.jmrix.can.cbus.eventtable.PackageTest.class
})

/**
 * Tests for the jmri.jmrix.can.cbus package.
 *
 * @author Bob Jacobsen Copyright 2008
 */
public class PackageTest  {
}
