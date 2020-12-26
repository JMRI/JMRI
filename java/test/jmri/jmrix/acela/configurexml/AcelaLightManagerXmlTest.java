package jmri.jmrix.acela.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * AcelaLightManagerXmlTest.java
 *
 * Test for the AcelaLightManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class AcelaLightManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("AcelaLightManagerXml constructor",new AcelaLightManagerXml());
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

