package jmri.jmrix.ecos.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * EcosReporterManagerXmlTest.java
 *
 * Test for the EcosReporterManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class EcosReporterManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("EcosReporterManagerXml constructor",new EcosReporterManagerXml());
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

