package jmri.jmrix.bidib.swing;

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
    jmri.jmrix.bidib.swing.mon.PackageTest.class,
    jmri.jmrix.bidib.swing.BiDiBComponentFactoryTest.class,
    jmri.jmrix.bidib.swing.BiDiBTableModelTest.class,
    jmri.jmrix.bidib.swing.BundleTest.class,
    jmri.jmrix.bidib.swing.BiDiBSignalMastAddPaneTest.class,
    jmri.jmrix.bidib.swing.BiDiBMenuTest.class,
    jmri.jmrix.bidib.swing.BiDiBNamedPaneActionTest.class,
})

public class PackageTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }
    
}
