package jmri.jmrix.bidib;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Tests for the jmri.jmrix.bidib package
 *
 * @author  Eckart Meyer  Copyright (C) 2020
 */

@RunWith(Suite.class)
@Suite.SuiteClasses({
    jmri.jmrix.bidib.BiDiBSystemConnectionMemoTest.class,
    jmri.jmrix.bidib.BiDiBConstantsTest.class,
    jmri.jmrix.bidib.BundleTest.class,
    jmri.jmrix.bidib.BiDiBConnectionTypeListTest.class,
    jmri.jmrix.bidib.BiDiBAddressTest.class,
    jmri.jmrix.bidib.BiDiBProgrammerManagerTest.class,
    jmri.jmrix.bidib.BiDiBPowerManagerTest.class,
    jmri.jmrix.bidib.BiDiBTurnoutTest.class,
    jmri.jmrix.bidib.BiDiBTurnoutManagerTest.class,
    jmri.jmrix.bidib.BiDiBSignalMastTest.class,
    jmri.jmrix.bidib.BiDiBLightTest.class,
    jmri.jmrix.bidib.BiDiBPredefinedMetersTest.class,
    jmri.jmrix.bidib.BiDiBReporterManagerTest.class,
    jmri.jmrix.bidib.BiDiBOutputMessageHandlerTest.class,
    jmri.jmrix.bidib.BiDiBThrottleTest.class,
    jmri.jmrix.bidib.BiDiBTrafficControllerTest.class,
    jmri.jmrix.bidib.BiDiBReporterTest.class,
    jmri.jmrix.bidib.BiDiBThrottleManagerTest.class,
    jmri.jmrix.bidib.BiDiBLightManagerTest.class,
    jmri.jmrix.bidib.BiDiBSensorManagerTest.class,
    jmri.jmrix.bidib.BiDiBOpsModeProgrammerTest.class,
    jmri.jmrix.bidib.BiDiBProgrammerTest.class,
    jmri.jmrix.bidib.BiDiBSensorTest.class, 
    jmri.jmrix.bidib.BiDiBNodeInitializerTest.class, 
    jmri.jmrix.bidib.configurexml.PackageTest.class,
    jmri.jmrix.bidib.serialdriver.PackageTest.class,
    jmri.jmrix.bidib.simulator.PackageTest.class,
    jmri.jmrix.bidib.swing.PackageTest.class,
    jmri.jmrix.bidib.tcpserver.PackageTest.class,
})

public class PackageTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }
    
}
