package jmri.jmrix.cmri.serial;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Tests for the jmri.jmrix.cmri.serial package.
 *
 * @author Bob Jacobsen Copyright 2003
 * @author Paul Bender Copyright (C) 2016
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
   jmri.jmrix.cmri.serial.SerialTurnoutManagerTest.class,
   jmri.jmrix.cmri.serial.SerialSensorManagerTest.class,
   jmri.jmrix.cmri.serial.SerialNodeTest.class,
   jmri.jmrix.cmri.serial.SerialNodeListTest.class,
   jmri.jmrix.cmri.serial.SerialMessageTest.class,
   jmri.jmrix.cmri.serial.SerialTrafficControllerTest.class,
   jmri.jmrix.cmri.serial.SerialAddressTest.class,
   jmri.jmrix.cmri.serial.SerialAddressTwoSystemTest.class,
   jmri.jmrix.cmri.serial.networkdriver.PackageTest.class,
   jmri.jmrix.cmri.serial.sim.PackageTest.class,
   jmri.jmrix.cmri.serial.serialdriver.PackageTest.class,
   jmri.jmrix.cmri.serial.configurexml.PackageTest.class,
   jmri.jmrix.cmri.serial.serialmon.PackageTest.class,
   jmri.jmrix.cmri.serial.nodeconfig.PackageTest.class,
   jmri.jmrix.cmri.serial.nodeiolist.PackageTest.class,
   jmri.jmrix.cmri.serial.assignment.PackageTest.class,
   jmri.jmrix.cmri.serial.diagnostic.PackageTest.class,
   jmri.jmrix.cmri.serial.packetgen.PackageTest.class,
   SerialNetworkPortAdapterTest.class,
   SerialPortAdapterTest.class,
   SerialReplyTest.class,
   SerialTurnoutTest.class,
   SerialLightTest.class,
   SerialLightManagerTest.class,
   SerialSensorTest.class,
   BundleTest.class,
   jmri.jmrix.cmri.serial.nodeconfigmanager.PackageTest.class,
   jmri.jmrix.cmri.serial.cmrinetmanager.PackageTest.class,
})

public class PackageTest{
}
