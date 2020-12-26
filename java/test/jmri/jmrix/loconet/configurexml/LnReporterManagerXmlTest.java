package jmri.jmrix.loconet.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * LnReporterManagerXmlTest.java
 *
 * Test for the LnReporterManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class LnReporterManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("LnReporterManagerXml constructor",new LnReporterManagerXml());
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

