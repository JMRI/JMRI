package jmri.jmrix.can.cbus.configurexml;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    CbusLightManagerXmlTest.class,
    CbusSensorManagerXmlTest.class,
    CbusReporterManagerXmlTest.class,
    CbusTurnoutManagerXmlTest.class
})
/**
 * Tests for the jmri.jmrix.can.cbus.configurexml package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
