package jmri.jmrix.can.adapters.gridconnect;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        GridConnectMessageTest.class,
        GridConnectReplyTest.class,
        jmri.jmrix.can.adapters.gridconnect.canrs.PackageTest.class,
        jmri.jmrix.can.adapters.gridconnect.lccbuffer.PackageTest.class,
        jmri.jmrix.can.adapters.gridconnect.net.PackageTest.class,
        jmri.jmrix.can.adapters.gridconnect.canusb.PackageTest.class,
        jmri.jmrix.can.adapters.gridconnect.can2usbino.PackageTest.class,
        GcPortControllerTest.class,
        GcTrafficControllerTest.class,
        GcSerialDriverAdapterTest.class,
})

/**
 * Tests for the jmri.jmrix.can.adapters.gridconnect package.
 *
 * @author Bob Jacobsen Copyright 2009
 */
public class PackageTest  {
}
