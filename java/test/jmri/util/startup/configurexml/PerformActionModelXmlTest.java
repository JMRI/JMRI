package jmri.util.startup.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * PerformActionModelXmlTest.java
 *
 * Test for the PerformActionModelXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class PerformActionModelXmlTest {

    @Test
    public void testCtor(){
      Assertions.assertNotNull( new PerformActionModelXml(), "PerformActionModelXml constructor");
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

