package jmri.jmrix.dccpp.configurexml;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    DCCppLightManagerXmlTest.class,
    DCCppSensorManagerXmlTest.class,
    DCCppTurnoutManagerXmlTest.class,
    DCCppStreamConnectionConfigXmlTest.class
})
/**
 * Tests for the jmri.jmrix.dccpp.configurexml package.
 *
 * @author Paul Bender Copyright (C) 2019
 */
public class PackageTest {
}
