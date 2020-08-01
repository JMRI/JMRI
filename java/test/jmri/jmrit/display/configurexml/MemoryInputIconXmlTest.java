package jmri.jmrit.display.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * MemoryInputIconXmlTest.java
 *
 * Test for the MemoryInputIconXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class MemoryInputIconXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("MemoryInputIconXml constructor",new MemoryInputIconXml());
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

