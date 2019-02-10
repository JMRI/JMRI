package jmri.jmrix.can.adapters.gridconnect.can2usbino;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   jmri.jmrix.can.adapters.gridconnect.can2usbino.serialdriver.PackageTest.class,
   GridConnectDoubledMessageTest.class
})
/**
 * Tests for the jmri.jmrix.can.adapters.gridconnect.can2usbino package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
