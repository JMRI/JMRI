package jmri.jmrix.rfid.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * RfidReporterManagerXmlTest.java
 *
 * Test for the RfidReporterManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class RfidReporterManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("RfidReporterManagerXml constructor",new RfidReporterManagerXml());
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

