package jmri.jmrix.internal.configurexml;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
   ConnectionConfigXmlTest.class,
   InternalLightManagerXmlTest.class,
   InternalReporterManagerXmlTest.class,
   InternalSensorManagerXmlTest.class,
   InternalTurnoutManagerXmlTest.class,
   LoadAndStoreTest.class
})
/**
 * Tests for the jmri.jmrix.internal.configurexml package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
