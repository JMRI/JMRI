package jmri.jmrix.bidib.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the BiDiBLightManagerXml class
 * 
 * @author  Eckart Meyer  Copyright (C) 2020
 */
public class BiDiBLightManagerXmlTest {

    @Test
    public void testCtor(){
        BiDiBLightManagerXml t = new BiDiBLightManagerXml();
        Assertions.assertNotNull(t, "BiDiBLightManagerXml constructor");
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
