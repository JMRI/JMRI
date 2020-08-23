package jmri.jmrit.display.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * MemorySpinnerIconXmlTest.java
 *
 * Test for the MemorySpinnerIconXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class MemorySpinnerIconXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("MemorySpinnerIconXml constructor",new MemorySpinnerIconXml());
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

