package jmri.jmrit.display.layoutEditor.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * LayoutBlockManagerXmlTest.java
 *
 * Test for the LayoutBlockManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class LayoutBlockManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("LayoutBlockManagerXml constructor",new LayoutBlockManagerXml());
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

