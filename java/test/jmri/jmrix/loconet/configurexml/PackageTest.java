package jmri.jmrix.loconet.configurexml;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    LNCPSignalMastXmlTest.class,
    LnLightManagerXmlTest.class,
    LnReporterManagerXmlTest.class,
    LnSensorManagerXmlTest.class,
    LnTurnoutManagerXmlTest.class,
    SE8cSignalHeadXmlTest.class,
    LoadAndStoreTest.class
})
/**
 * Tests for the jmri.jmrix.loconet.configurexml package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
