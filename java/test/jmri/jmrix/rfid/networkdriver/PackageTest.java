package jmri.jmrix.rfid.networkdriver;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
   ConnectionConfigTest.class,
   jmri.jmrix.rfid.networkdriver.configurexml.PackageTest.class,
   NetworkDriverAdapterTest.class,
   BundleTest.class,
})
/**
 * Tests for the jmri.jmrix.pi package
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
