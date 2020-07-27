package jmri.jmrit.display.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * MultiSensorIconXmlTest.java
 *
 * Test for the MultiSensorIconXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class MultiSensorIconXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("MultiSensorIconXml constructor",new MultiSensorIconXml());
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

