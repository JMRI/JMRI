package jmri.jmrix.pi.configurexml;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    RaspberryPiConnectionConfigXmlTest.class,
    RaspberryPiSensorManagerXmlTest.class,
    RaspberryPiTurnoutManagerXmlTest.class,
    RaspberryPiClassMigrationTest.class
})
/**
 * Tests for the jmri.jmrix.pi.configurexml package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
