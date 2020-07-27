package jmri.jmrix.lenz.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * XNetSensorManagerXmlTest.java
 *
 * Test for the XNetSensorManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class XNetSensorManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("XNetSensorManagerXml constructor",new XNetSensorManagerXml());
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

