package jmri.util.startup.configurexml;

import jmri.util.JUnitUtil;

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
      Assertions.assertNotNull( new PerformScriptModelXml(), "PerformScriptModelXml constructor");
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

