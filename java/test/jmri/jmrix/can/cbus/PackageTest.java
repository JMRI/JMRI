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
        CbusReporterManagerTest.class,
        CbusConstantsTest.class,
        CbusEventFilterTest.class,
        CbusMessageTest.class,
        CbusOpCodesTest.class,
        CbusCommandStationTest.class,
        CbusConfigurationManagerTest.class,
        CbusDccProgrammerManagerTest.class,
        CbusDccOpsModeProgrammerTest.class,
        CbusDccProgrammerTest.class,
        CbusLightManagerTest.class,
        CbusLightTest.class,
        CbusPowerManagerTest.class,
        CbusReporterTest.class,
        CbusThrottleTest.class,
        CbusThrottleManagerTest.class,
        CbusTurnoutManagerTest.class,
        CbusTurnoutTest.class,
        BundleTest.class,
})

/**
 * Tests for the jmri.jmrix.can.cbus package.
 *
 * @author Bob Jacobsen Copyright 2008
 */
public class PackageTest  {
}
