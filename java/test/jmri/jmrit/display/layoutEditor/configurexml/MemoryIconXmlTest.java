package jmri.jmrit.display.layoutEditor.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * MemoryIconXmlTest.java
 *
 * Test for the MemoryIconXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class MemoryIconXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("MemoryIconXml constructor",new MemoryIconXml());
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

