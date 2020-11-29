package jmri.util.startup.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
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
      Assert.assertNotNull("PerformActionModelXml constructor",new PerformActionModelXml());
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

