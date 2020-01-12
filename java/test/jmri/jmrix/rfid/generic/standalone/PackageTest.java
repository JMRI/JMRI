package jmri.jmrix.rfid.generic.standalone;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    jmri.jmrix.rfid.generic.standalone.configurexml.PackageTest.class,
    StandaloneReporterManagerTest.class,
    StandaloneSystemConnectionMemoTest.class,
    StandaloneTrafficControllerTest.class,
    StandaloneSensorManagerTest.class,
    StandaloneMessageTest.class,
    StandaloneReplyTest.class
})
/**
 * Tests for the jmri.jmrix.rfid.generic.standalone package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
