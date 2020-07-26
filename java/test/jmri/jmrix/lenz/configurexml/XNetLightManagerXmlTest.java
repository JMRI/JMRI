package jmri.jmrix.lenz.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * XNetLightManagerXmlTest.java
 *
 * Test for the XNetLightManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class XNetLightManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("XNetLightManagerXml constructor",new XNetLightManagerXml());
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

