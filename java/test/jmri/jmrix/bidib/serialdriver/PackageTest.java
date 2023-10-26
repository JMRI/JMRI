package jmri.jmrix.bidib.serialdriver;

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
    jmri.jmrix.bidib.serialdriver.ConnectionConfigTest.class,
    jmri.jmrix.bidib.serialdriver.BundleTest.class,
    jmri.jmrix.bidib.serialdriver.SerialDriverAdapterTest.class,
    jmri.jmrix.bidib.serialdriver.configurexml.PackageTest.class,
})

public class PackageTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }
    
}
