package jmri.jmrix.rfid.generic.standalone.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * StandaloneReporterManagerXmlTest.java
 *
 * Test for the StandaloneReporterManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class StandaloneReporterManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("StandaloneReporterManagerXml constructor",new StandaloneReporterManagerXml());
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

