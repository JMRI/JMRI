package jmri.jmrit.display.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * SensorIconXmlTest.java
 *
 * Test for the SensorIconXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class SensorIconXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("SensorIconXml constructor",new SensorIconXml());
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

