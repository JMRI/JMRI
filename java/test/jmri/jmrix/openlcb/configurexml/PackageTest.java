package jmri.jmrix.openlcb.configurexml;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    OlcbLightManagerXmlTest.class,
    OlcbSensorManagerXmlTest.class,
    OlcbSignalMastXmlTest.class,
    OlcbTurnoutManagerXmlTest.class,
    ProtocolOptionsPersistenceTest.class,
    SchemaTest.class,
    LoadAndStoreTest.class
})
/**
 * Tests for the jmri.jmrix.openlcb.configurexml package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
