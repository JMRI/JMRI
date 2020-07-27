package jmri.jmrix.dccpp.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * DCCppLightManagerXmlTest.java
 *
 * Test for the DCCppLightManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class DCCppLightManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("DCCppLightManagerXml constructor",new DCCppLightManagerXml());
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

