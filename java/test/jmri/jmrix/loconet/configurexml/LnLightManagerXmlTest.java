package jmri.jmrix.loconet.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * LnLightManagerXmlTest.java
 *
 * Test for the LnLightManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class LnLightManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("LnLightManagerXml constructor",new LnLightManagerXml());
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

