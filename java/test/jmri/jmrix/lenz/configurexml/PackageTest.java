package jmri.jmrix.lenz.configurexml;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   XNetLightManagerXmlTest.class,
   XNetSensorManagerXmlTest.class,
   XNetTurnoutManagerXmlTest.class,
   AbstractXNetSerialConnectionConfigXmlTest.class,
   XNetStreamConnectionConfigXmlTest.class
})
/**
 * Tests for the jmri.jmrix.lenz.configurexml package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
