package jmri.jmrix.bidib.configurexml;

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
    jmri.jmrix.bidib.configurexml.BiDiBLightManagerXmlTest.class,
    jmri.jmrix.bidib.configurexml.BiDiBSensorManagerXmlTest.class,
    jmri.jmrix.bidib.configurexml.BiDiBTurnoutManagerXmlTest.class,
    jmri.jmrix.bidib.configurexml.BiDiBReporterManagerXmlTest.class,
    jmri.jmrix.bidib.configurexml.BiDiBSignalMastXmlTest.class,
})

public class PackageTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }
    
}
