package jmri.util.startup.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * PerformFileModelXmlTest.java
 *
 * Test for the PerformFileModelXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class PerformFileModelXmlTest {

    @Test
    public void testCtor(){
      Assertions.assertNotNull( new PerformFileModelXml(), "PerformFileModelXml constructor");
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

