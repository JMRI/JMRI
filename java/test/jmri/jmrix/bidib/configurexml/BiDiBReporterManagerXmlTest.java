package jmri.jmrix.bidib.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the BiDiBReporterManagerXml class
 * 
 * @author  Eckart Meyer  Copyright (C) 2020
 */
public class BiDiBReporterManagerXmlTest {

    @Test
    public void testCtor(){
        BiDiBReporterManagerXml t = new BiDiBReporterManagerXml();
        Assertions.assertNotNull(t, "BiDiBReporterManagerXml constructor");
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
