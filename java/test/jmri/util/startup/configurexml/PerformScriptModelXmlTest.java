package jmri.util.startup.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * PerformScriptModelXmlTest.java
 *
 * Test for the PerformScriptModelXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class PerformScriptModelXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("PerformScriptModelXml constructor",new PerformScriptModelXml());
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

