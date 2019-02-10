package jmri.jmrix.ecos.configurexml;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    EcosLocoAddressManagerXmlTest.class,
    EcosPreferencesXmlTest.class,
    EcosReporterManagerXmlTest.class,
    EcosSensorManagerXmlTest.class,
    EcosTurnoutManagerXmlTest.class
})
/**
 * Tests for the jmri.jmrix.ecos.configurexml package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
