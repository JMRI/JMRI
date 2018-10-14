package jmri.jmrix.rfid.swing;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   jmri.jmrix.rfid.swing.serialmon.PackageTest.class,
   RfidMenuTest.class,
   RfidComponentFactoryTest.class,
   RfidNamedPaneActionTest.class,
   RfidPanelTest.class,
   BundleTest.class,
})
/**
 * Tests for the jmri.jmrix.rfid.swing package
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
