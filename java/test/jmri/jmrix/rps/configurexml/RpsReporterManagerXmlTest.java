package jmri.jmrix.rps.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * RpsReporterManagerXmlTest.java
 *
 * Test for the RpsReporterManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class RpsReporterManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("RpsReporterManagerXml constructor",new RpsReporterManagerXml());
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

