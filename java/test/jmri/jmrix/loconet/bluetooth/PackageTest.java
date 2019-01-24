package jmri.jmrix.loconet.bluetooth;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   ConnectionConfigTest.class,
   jmri.jmrix.loconet.bluetooth.configurexml.PackageTest.class,
   LocoNetBluetoothAdapterTest.class,
   BundleTest.class,
})
/**
 * Tests for the jmri.jmrix.loconet.bluetooth package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
