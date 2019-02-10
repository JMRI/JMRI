package jmri.jmrix.rps.serial;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   SerialAdapterTest.class,
   ConnectionConfigTest.class,
   jmri.jmrix.rps.serial.configurexml.PackageTest.class

})
/**
 * Tests for the jmri.jmrix.rps.serial package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {

}
