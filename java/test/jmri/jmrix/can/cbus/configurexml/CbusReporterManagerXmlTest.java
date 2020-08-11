package jmri.jmrix.can.cbus.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

