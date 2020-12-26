package jmri.jmrix.rfid.merg.concentrator.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * ConcentratorReporterManagerXmlTest.java
 *
 * Test for the ConcentratorReporterManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class ConcentratorReporterManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("ConcentratorReporterManagerXml constructor",new ConcentratorReporterManagerXml());
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

