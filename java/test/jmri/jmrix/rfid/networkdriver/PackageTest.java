package jmri.jmrix.rfid.networkdriver;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RunWith(Suite.class)
@Suite.SuiteClasses({
   ConnectionConfigTest.class,
   jmri.jmrix.rfid.networkdriver.configurexml.PackageTest.class
})
/**
 * Tests for the jmri.jmrix.pi package
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
