package jmri.jmrix.loconet.usb_dcs52;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        BundleTest.class,
        jmri.jmrix.loconet.usb_dcs52.configurexml.PackageTest.class,
        jmri.jmrix.loconet.usb_dcs52.swing.PackageTest.class,
        UsbDcs52AdapterTest.class,
        UsbDcs52SystemConnectionMemoTest.class,
})

/**
 * Tests for the jmri.jmrix.loconet.usb_dcs52 package.
 *
 * @author	Bob Jacobsen Copyright 2001, 2003, 2006, 2008
 */
public class PackageTest  {
}
