package jmri.jmrix.rfid;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Suite.class)
@Suite.SuiteClasses({
     RfidStreamPortControllerTest.class,
     RfidSystemConnectionMemoTest.class,
     RfidSensorTest.class,
     RfidReporterTest.class,
     jmri.jmrix.rfid.protocol.coreid.CoreIdRfidProtocolTest.class,
     jmri.jmrix.rfid.protocol.em18.Em18RfidProtocolTest.class,
     jmri.jmrix.rfid.protocol.olimex.OlimexRfidProtocolTest.class,
     jmri.jmrix.rfid.protocol.parallax.ParallaxRfidProtocolTest.class,
     jmri.jmrix.rfid.protocol.seeedstudio.SeeedStudioRfidProtocolTest.class,
     jmri.jmrix.rfid.serialdriver.PackageTest.class,
     jmri.jmrix.rfid.networkdriver.PackageTest.class,
     jmri.jmrix.rfid.configurexml.PackageTest.class,
     jmri.jmrix.rfid.generic.PackageTest.class,
     jmri.jmrix.rfid.merg.PackageTest.class,
     RfidConnectionTypeListTest.class,
     RfidMessageTest.class,
     RfidReplyTest.class,
     RfidTrafficControllerTest.class,
     RfidReporterManagerTest.class,
     RfidSensorManagerTest.class,
     TimeoutRfidSensorTest.class,
     TimeoutRfidReporterTest.class,
     jmri.jmrix.rfid.swing.PackageTest.class
})

/**
 * tests for the jmri.jmrix.rfid package
 *
 * @author	Bob Jacobsen
 * @author      Paul Bender Copyright (C) 2016	
 */
public class PackageTest {


}
