package jmri.jmrix.jmriclient.configurexml;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   JMRIClientLightManagerXmlTest.class,
   JMRIClientReporterManagerXmlTest.class,
   JMRIClientSensorManagerXmlTest.class,
   JMRIClientTurnoutManagerXmlTest.class
})
/**
 * Tests for the jmri.jmrix.jmriclient.configurexml package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
