package jmri.jmrix.ieee802154.xbee;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        XBeeMessageTest.class,
        XBeeReplyTest.class,
        XBeeConnectionMemoTest.class,
        XBeeTrafficControllerTest.class,
        XBeeNodeTest.class,
        XBeeSensorManagerTest.class,
        XBeeSensorTest.class,
        XBeeLightManagerTest.class,
        XBeeLightTest.class,
        XBeeTurnoutManagerTest.class,
        XBeeTurnoutTest.class,
        XBeeAdapterTest.class,
        XBeeNodeManagerTest.class,
        XBeeIOStreamTest.class,
        ConnectionConfigTest.class,
        jmri.jmrix.ieee802154.xbee.configurexml.PackageTest.class,
        BundleTest.class,
        jmri.jmrix.ieee802154.xbee.swing.PackageTest.class
})


/**
 * Tests for the jmri.jmrix.ieee802154.xbee package
 *
 * @author	Paul Bender Copyright (C) 2012,2016
 */
public class PackageTest {
}
