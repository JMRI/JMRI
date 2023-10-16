package jmri.jmrix.bidib.simulator;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Tests for the jmri.jmrix.bidib package
 *
 * @author  Eckart Meyer  Copyright (C) 2020
 */

@RunWith(Suite.class)
@Suite.SuiteClasses({
    jmri.jmrix.bidib.simulator.BiDiBSimulatorAdapterTest.class,
    jmri.jmrix.bidib.simulator.ConnectionConfigTest.class,
    jmri.jmrix.bidib.simulator.BundleTest.class,
    jmri.jmrix.bidib.simulator.configurexml.PackageTest.class,
})

public class PackageTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }
    
}
