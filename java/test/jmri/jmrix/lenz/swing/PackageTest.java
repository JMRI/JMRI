package jmri.jmrix.lenz.swing;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
   jmri.jmrix.lenz.swing.liusb.PackageTest.class,
   jmri.jmrix.lenz.swing.li101.PackageTest.class,
   jmri.jmrix.lenz.swing.mon.PackageTest.class,
   jmri.jmrix.lenz.swing.stackmon.PackageTest.class,
   jmri.jmrix.lenz.swing.systeminfo.PackageTest.class,
   jmri.jmrix.lenz.swing.packetgen.PackageTest.class,
   jmri.jmrix.lenz.swing.lz100.PackageTest.class,
   jmri.jmrix.lenz.swing.lzv100.PackageTest.class,
   jmri.jmrix.lenz.swing.lv102.PackageTest.class,
   BundleTest.class,
   XNetMenuTest.class,
   XNetComponentFactoryTest.class,
   AbstractXPressNetActionTest.class
})
/**
 * Tests for the jmri.jmrix.lenz.swing package
 *
 * @author	Bob Jacobsen
 * @author      Paul Bender Copyright (C) 2016
 */
public class PackageTest {


}
