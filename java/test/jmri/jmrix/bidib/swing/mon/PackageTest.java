package jmri.jmrix.bidib.swing.mon;

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
@Suite.SuiteClasses({jmri.jmrix.bidib.swing.mon.BundleTest.class, jmri.jmrix.bidib.swing.mon.BiDiBMonPaneTest.class})
public class PackageTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }
    
}
