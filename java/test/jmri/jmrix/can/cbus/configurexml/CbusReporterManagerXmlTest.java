package jmri.jmrix.can.cbus.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * CbusReporterManagerXmlTest.java
 *
 * Test for the CbusReporterManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class CbusReporterManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("CbusReporterManagerXml constructor",new CbusReporterManagerXml());
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

