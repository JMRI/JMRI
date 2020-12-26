package jmri.jmrix.nce.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * NceSensorManagerXmlTest.java
 *
 * Test for the NceSensorManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class NceSensorManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("NceSensorManagerXml constructor",new NceSensorManagerXml());
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

