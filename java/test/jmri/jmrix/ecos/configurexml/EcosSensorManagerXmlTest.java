package jmri.jmrix.ecos.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * EcosSensorManagerXmlTest.java
 *
 * Test for the EcosSensorManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class EcosSensorManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("EcosSensorManagerXml constructor",new EcosSensorManagerXml());
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

