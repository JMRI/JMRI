package jmri.jmrit.display.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * LightIconXmlTest.java
 *
 * Test for the LightIconXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class LightIconXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("LightIconXml constructor",new LightIconXml());
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

