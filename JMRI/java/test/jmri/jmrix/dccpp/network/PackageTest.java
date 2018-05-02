package jmri.jmrix.dccpp.network;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        DCCppEthernetAdapterTest.class,
        DCCppEthernetPacketizerTest.class,
        ConnectionConfigTest.class,
        jmri.jmrix.dccpp.network.configurexml.PackageTest.class,
})

/**
 * Tests for the jmri.jmrix.dccpp.network package
 *
 * @author Paul Bender
 * @author Mark Underwood Copyright (C) 2015
 */
public class PackageTest  {
}
