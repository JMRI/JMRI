package jmri.jmrix.dcc4pc.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Dcc4PcReporterManagerXmlTest.java
 *
 * Test for the Dcc4PcReporterManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class Dcc4PcReporterManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("Dcc4PcReporterManagerXml constructor",new Dcc4PcReporterManagerXml());
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

