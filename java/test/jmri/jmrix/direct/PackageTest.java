package jmri.jmrix.direct;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        jmri.jmrix.direct.MakePacketTest.class,
        jmri.jmrix.direct.serial.PackageTest.class,
        jmri.jmrix.direct.configurexml.PackageTest.class,
        PortControllerTest.class,
        TrafficControllerTest.class,
        DirectMenuTest.class,
        ThrottleManagerTest.class,
        ThrottleTest.class,
        MessageTest.class,
        jmri.jmrix.direct.swing.PackageTest.class,
        DirectSystemConnectionMemoTest.class,
})

/**
 * Tests for the jmri.jmrix.direct package.
 *
 * @author Bob Jacobsen Copyright 2004
 */
public class PackageTest  {
}
