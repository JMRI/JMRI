package jmri.jmrix.loconet.usb_dcs240;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        BundleTest.class,
        jmri.jmrix.loconet.usb_dcs240.configurexml.PackageTest.class,
        jmri.jmrix.loconet.usb_dcs240.swing.PackageTest.class,
        UsbDcs240AdapterTest.class,
        UsbDcs240SystemConnectionMemoTest.class,
})

/**
 * Tests for the jmri.jmrix.loconet.usb_dcs240 package.
 *
 * @author	Bob Jacobsen Copyright 2001, 2003, 2006, 2008
 */
public class PackageTest  {
}
