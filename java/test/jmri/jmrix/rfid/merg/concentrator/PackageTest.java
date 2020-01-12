package jmri.jmrix.rfid.merg.concentrator;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    jmri.jmrix.rfid.merg.concentrator.configurexml.PackageTest.class,
    ConcentratorMessageTest.class,
    ConcentratorReplyTest.class,
    ConcentratorReporterManagerTest.class,
    ConcentratorSensorManagerTest.class,
    ConcentratorSystemConnectionMemoTest.class,
    ConcentratorTrafficControllerTest.class
})
/**
 * Tests for the jmri.jmrix.rfid.merg.concentrator package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
