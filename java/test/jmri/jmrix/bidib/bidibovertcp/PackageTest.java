package jmri.jmrix.bidib.bidibovertcp;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Tests for the jmri.jmrix.bidib package
 *
 * @author  Eckart Meyer  Copyright (C) 2023
 */

@RunWith(Suite.class)
@Suite.SuiteClasses({
    jmri.jmrix.bidib.bidibovertcp.BiDiBOverTcpAdapterTest.class,
    jmri.jmrix.bidib.bidibovertcp.ConnectionConfigTest.class,
    jmri.jmrix.bidib.bidibovertcp.BundleTest.class,
    jmri.jmrix.bidib.bidibovertcp.configurexml.PackageTest.class,
})

public class PackageTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }
    
}
