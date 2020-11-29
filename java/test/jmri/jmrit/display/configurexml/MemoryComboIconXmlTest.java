package jmri.jmrit.display.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * MemoryComboIconXmlTest.java
 *
 * Test for the MemoryComboIconXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class MemoryComboIconXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("MemoryComboIconXml constructor",new MemoryComboIconXml());
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

