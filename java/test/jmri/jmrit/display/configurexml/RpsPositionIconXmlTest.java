package jmri.jmrit.display.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * RpsPositionIconXmlTest.java
 *
 * Test for the RpsPositionIconXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class RpsPositionIconXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("RpsPositionIconXml constructor",new RpsPositionIconXml());
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

